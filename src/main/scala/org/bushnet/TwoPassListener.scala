package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.apache.logging.log4j.LogManager

import scala.collection.mutable.Map
import scala.collection.convert.WrapAsScala._

import AddrMode._

class TwoPassListener extends AssemblerBaseListener {
  val StringDecode = """"(.*)"""".r
  val log = LogManager.getLogger(this.getClass)
  val symbols = Map[String,Int]("**" -> 0)
  var memoryStart = 0
  var memorySize = 0x8000
  var memory = Array.fill(memorySize)(0xff)
  var op:Operation.Value = _
  var addrMode:AddrMode.Value = _
  val exprValues = new ParseTreeProperty[Int] {
    def apply(node: ParseTree) = { get(node) }
    def update(node: ParseTree, value: Int) = { put(node, value) }
    def clear() = { annotations.clear() }
    override def toString = annotations.keySet().map(key => s"${key.getText} => ${annotations.get(key)}").mkString("{", ", ", "}")
  }
  val argValues = new ParseTreeProperty[List[Int]] {
    def apply(node: ParseTree) = { get(node) }
    def update(node: ParseTree, value: List[Int]) = { put(node, value) }
    def clear() = { annotations.clear() }
    override def toString = annotations.keySet().map(key => s"${key.getText} => ${annotations.get(key)}").mkString("{", ", ", "}")
  }
  implicit def stringToInt(s: String) = s.charAt(0) match {
    case '$' => Integer.parseInt(s.tail, 16)
    case '@' => Integer.parseInt(s.tail, 8)
    case '%' => Integer.parseInt(s.tail, 2)
    case '\'' => s.charAt(1).toInt
    case _ => s.toInt
  }
  def pc = symbols.getOrElse("**", 0xffff)
  def store(byte: Int) {
    val address = symbols("**")
    val offset = address - memoryStart
    if (offset < 0 || offset >= memorySize) {
      log.error(s"Address out of bounds: ${address} (expecting in range ${memoryStart} - ${memoryStart+memorySize-1})")
    } else {
      memory(offset) = byte & 0xff
      symbols("**") += 1
    }
  }

  override def enterProg(ctx: AssemblerParser.ProgContext) { }
  override def exitProg(ctx: AssemblerParser.ProgContext) { }
  override def enterLine(ctx: AssemblerParser.LineContext) { }
  override def exitLine(ctx: AssemblerParser.LineContext) {
    exprValues.clear()
  }
  override def enterStatement(ctx: AssemblerParser.StatementContext) { }
  override def exitStatement(ctx: AssemblerParser.StatementContext) { }
  override def enterLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) {
    val label = ctx.ID()
    val labelPart = if (label == null) "" else s"${label.getText} "
    ctx.command() match {
      case directiveCommand:AssemblerParser.DirectiveCommandContext =>
        val directive = directiveCommand.DIRECTIVE().getText
        val args = directiveCommand.args() match {
          case string:AssemblerParser.StringArgContext =>
            string.STRING().getText
          case argList:AssemblerParser.ListArgContext =>
            argList.expr().map(_.getText).mkString(", ")
          case _ =>
            ""
        }
        log.debug(s"${labelPart}${directive} ${args}")
      case opCommand:AssemblerParser.OpCommandContext =>
        val operand = opCommand.operand()
        val operandPart = if (operand == null) "" else operand.getText
        log.debug(s"${labelPart}${opCommand.OPCODE().getText} ${operandPart}")
      case x =>
        log.error(s"Unknown command type: ${x.getClass.getName}")
    }
    if (label != null) {
      log.debug(s"${label.getText} := ${pc}")
      symbols(label.getText) = pc
    }
  }
  override def exitLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) { }
  override def enterOpCommand(ctx: AssemblerParser.OpCommandContext) {
    addrMode = Implied
    val opName = ctx.OPCODE().getText
    op = Operation.withName(opName)
  }
  override def exitOpCommand(ctx: AssemblerParser.OpCommandContext) {
    val opName = ctx.OPCODE().getText
    val operand = ctx.operand()
    val addr = if (operand == null) -1 else exprValues(operand)
    val opCode = OpCodes(op, addrMode)
    log.debug(s"${opName} -> ${op} addr: ${addr}")
    store(opCode.opCode)
    if (opCode.bytes > 0) {
      store(addr)
      if (opCode.bytes > 1) {
        store(addr / 256)
      }
    }
  }
  override def enterDirectiveCommand(ctx: AssemblerParser.DirectiveCommandContext) { }
  override def exitDirectiveCommand(ctx: AssemblerParser.DirectiveCommandContext) {
    val directive = ctx.DIRECTIVE().getText
    val args = argValues(ctx.args())
    log.debug(s"Directive ${directive} ${args}")
    directive match {
      case ".OUTPUT" =>
        if (args.size != 2) {
          log.error(s"OUTPUT expected 2 args, got ${args.size}")
        } else {
          val newStart = args(0)
          val newSize = args(1)-memoryStart+1
          if (newStart != memoryStart || newSize != memorySize) {
            memoryStart = newStart
            memorySize = newSize
            memory = Array.fill(memorySize)(0xff)
          }
        }
      case ".BYTE" =>
        args.foreach { byte => store(byte) }
      case ".DBYTE" =>
        // Store in high byte, low byte order
        args.foreach { word =>
          store(word / 256)
          store(word % 256)
        }
      case ".WORD" =>
        // Store in low byte, high byte order
        args.foreach { word =>
          store(word % 256)
          store(word / 256)
        }
      case _ =>
        log.error(s"Unknown directive: ${directive}")
    }
  }
  override def enterOperand(ctx: AssemblerParser.OperandContext) { }
  override def exitOperand(ctx: AssemblerParser.OperandContext) {
    exprValues(ctx) = exprValues(ctx.getChild(0))
  }
  override def enterImmediateAddr(ctx: AssemblerParser.ImmediateAddrContext) { }
  override def exitImmediateAddr(ctx: AssemblerParser.ImmediateAddrContext) {
    addrMode = Immediate
    exprValues(ctx) = exprValues(ctx.expr())
  }
  override def enterDirectAddr(ctx: AssemblerParser.DirectAddrContext) { }
  override def exitDirectAddr(ctx: AssemblerParser.DirectAddrContext) {
    val target = exprValues(ctx.expr())
    exprValues(ctx) = target
    addrMode = if (Operation.relativeAddrOps.contains(op)) {
      val currentAddr = pc
      val relAddr = target - (currentAddr + 2)
      exprValues(ctx) = if (relAddr >= 0) relAddr else relAddr+256
      Relative
    } else if (target >= 256 || !OpCodes.has(op, ZeroPage)) {
      Absolute 
    } else {
      ZeroPage
    }
  }
  override def enterIndexedDirectAddrX(ctx: AssemblerParser.IndexedDirectAddrXContext) { }
  override def exitIndexedDirectAddrX(ctx: AssemblerParser.IndexedDirectAddrXContext) {
    val value = exprValues(ctx.expr())
    exprValues(ctx) = value
    addrMode = if (value >= 256) AbsoluteIndexedX else ZeroPageIndexedX
  }
  override def enterIndexedDirectAddrY(ctx: AssemblerParser.IndexedDirectAddrYContext) { }
  override def exitIndexedDirectAddrY(ctx: AssemblerParser.IndexedDirectAddrYContext) {
    val value = exprValues(ctx.expr())
    exprValues(ctx) = value
    addrMode = if (value >= 256) AbsoluteIndexedY else ZeroPageIndexedY
  }
  override def enterIndirectAddr(ctx: AssemblerParser.IndirectAddrContext) { }
  override def exitIndirectAddr(ctx: AssemblerParser.IndirectAddrContext) {
    val value = exprValues(ctx.expr())
    exprValues(ctx) = value
    addrMode = if (value >= 256) AbsoluteIndirect else ZeroPageIndirect
  }
  override def enterPreIndexedIndirectAddr(ctx: AssemblerParser.PreIndexedIndirectAddrContext) { }
  override def exitPreIndexedIndirectAddr(ctx: AssemblerParser.PreIndexedIndirectAddrContext) {
    val value = exprValues(ctx.expr())
    exprValues(ctx) = value
    addrMode = ZeroPagePreIndexedIndirect
  }
  override def enterPostIndexedIndirectAddr(ctx: AssemblerParser.PostIndexedIndirectAddrContext) { }
  override def exitPostIndexedIndirectAddr(ctx: AssemblerParser.PostIndexedIndirectAddrContext) {
    val value = exprValues(ctx.expr())
    exprValues(ctx) = value
    addrMode = ZeroPagePostIndexedIndirect
  }
  override def enterStringArg(ctx: AssemblerParser.StringArgContext) { }
  override def exitStringArg(ctx: AssemblerParser.StringArgContext) {
    ctx.STRING().getText match {
      case StringDecode(rawString) =>
        val values = rawString.getBytes.map(_.toInt).toList
        log.debug(s"${values}")
        argValues(ctx) = values
      case other =>
        log.error(s"Unexpected string format: ${other}")
    }
  }
  override def enterListArg(ctx: AssemblerParser.ListArgContext) { }
  override def exitListArg(ctx: AssemblerParser.ListArgContext) {
    val values = ctx.expr().map(exprValues(_)).toList
    log.debug(s"${values}")
    argValues(ctx) = values
  }
  override def enterVarAssign(ctx: AssemblerParser.VarAssignContext) {
    log.debug(s"${ctx.ID().getText} = ${ctx.expr().getText}")
  }
  override def exitVarAssign(ctx: AssemblerParser.VarAssignContext) {
    log.debug(s"${ctx.ID().getText} := ${exprValues(ctx.expr())}")
    symbols(ctx.ID().getText) = exprValues(ctx.expr())
  }
  override def enterPcAssign(ctx: AssemblerParser.PcAssignContext) {
    log.debug(s"** = ${ctx.expr().getText}")
  }
  override def exitPcAssign(ctx: AssemblerParser.PcAssignContext) {
    log.debug(s"** := ${exprValues(ctx.expr())}")
    symbols("**") = exprValues(ctx.expr())
  }
  override def enterDiv(ctx: AssemblerParser.DivContext) { }
  override def exitDiv(ctx: AssemblerParser.DivContext) {
    log.debug(s"${exprValues(ctx.expr(0))} / ${exprValues(ctx.expr(1))}")
    exprValues(ctx) = exprValues(ctx.expr(0)) / exprValues(ctx.expr(1))
  }
  override def enterAdd(ctx: AssemblerParser.AddContext) { }
  override def exitAdd(ctx: AssemblerParser.AddContext) {
    log.debug(s"${exprValues(ctx.expr(0))} + ${exprValues(ctx.expr(1))}")
    exprValues(ctx) = exprValues(ctx.expr(0)) + exprValues(ctx.expr(1))
  }
  override def enterSub(ctx: AssemblerParser.SubContext) { }
  override def exitSub(ctx: AssemblerParser.SubContext) {
    log.debug(s"${exprValues(ctx.expr(0))} - ${exprValues(ctx.expr(1))}")
    exprValues(ctx) = exprValues(ctx.expr(0)) - exprValues(ctx.expr(1))
  }
	override def enterPc(ctx: AssemblerParser.PcContext) { }
	override def exitPc(ctx: AssemblerParser.PcContext) {
    log.debug(s"** -> ${pc}")
    exprValues(ctx) = pc
	}
  override def enterMult(ctx: AssemblerParser.MultContext) { }
  override def exitMult(ctx: AssemblerParser.MultContext) {
    log.debug(s"${exprValues(ctx.expr(0))} * ${exprValues(ctx.expr(1))}")
    exprValues(ctx) = exprValues(ctx.expr(0)) * exprValues(ctx.expr(1))
  }
  override def enterHighByte(ctx: AssemblerParser.HighByteContext) { }
  override def exitHighByte(ctx: AssemblerParser.HighByteContext) {
    log.debug(s">${exprValues(ctx.expr())}")
    exprValues(ctx) = exprValues(ctx.expr()) / 256
  }
  override def enterVar(ctx: AssemblerParser.VarContext) { }
  override def exitVar(ctx: AssemblerParser.VarContext) {
    log.debug(s"${ctx.ID().getText} -> ${symbols.getOrElse(ctx.ID().getText, 0xffff)}")
    exprValues(ctx) = symbols.getOrElse(ctx.ID().getText, 0xffff)
  }
  override def enterChar(ctx: AssemblerParser.CharContext) { }
  override def exitChar(ctx: AssemblerParser.CharContext) {
    log.debug(s"Char ${ctx.CHAR().getText} = ${ctx.CHAR().getText.charAt(1).toInt}")
    exprValues(ctx) = ctx.CHAR().getText
  }
  override def enterParens(ctx: AssemblerParser.ParensContext) { }
  override def exitParens(ctx: AssemblerParser.ParensContext) {
    log.debug(s"[${exprValues(ctx.expr())}]")
    exprValues(ctx) = exprValues(ctx.expr())
  }
  override def enterNum(ctx: AssemblerParser.NumContext) { }
  override def exitNum(ctx: AssemblerParser.NumContext) {
    log.debug(s"Num ${exprValues(ctx.getChild(0))}")
    exprValues(ctx) = exprValues(ctx.getChild(0))
  }
  override def enterRem(ctx: AssemblerParser.RemContext) { }
  override def exitRem(ctx: AssemblerParser.RemContext) {
    log.debug(s"${exprValues(ctx.expr(0))} % ${exprValues(ctx.expr(1))}")
    exprValues(ctx) = exprValues(ctx.expr(0)) % exprValues(ctx.expr(1))
  }
  override def enterLowByte(ctx: AssemblerParser.LowByteContext) { }
  override def exitLowByte(ctx: AssemblerParser.LowByteContext) {
    log.debug(s"<${exprValues(ctx.expr())}")
    exprValues(ctx) = exprValues(ctx.expr()) % 256
  }
  override def enterNumber(ctx: AssemblerParser.NumberContext) { }
  override def exitNumber(ctx: AssemblerParser.NumberContext) {
    log.debug(s"number ${ctx.getChild(0).getText}")
    exprValues(ctx) = ctx.getChild(0).getText
  }
  override def enterEveryRule(ctx: ParserRuleContext) { }
  override def exitEveryRule(ctx: ParserRuleContext) { }
  override def visitTerminal(node: TerminalNode) { }
  override def visitErrorNode(node: ErrorNode) { }
}