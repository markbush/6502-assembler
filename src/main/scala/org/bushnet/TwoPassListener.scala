package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.apache.log4j.Logger

import scala.collection.mutable.Map
import scala.collection.convert.WrapAsScala._

class TwoPassListener extends AssemblerBaseListener {
  val log = Logger.getLogger(this.getClass)
  var memory = new Array(32768)
  var symbols = Map[String,Int]("**" -> 0)
  var memoryStart = 0
  val values = new ParseTreeProperty[Int] {
    def apply(node: ParseTree) = { get(node) }
    def update(node: ParseTree, value: Int) = { put(node, value) }
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

  override def enterProg(ctx: AssemblerParser.ProgContext) { }
  override def exitProg(ctx: AssemblerParser.ProgContext) { }
  override def enterLine(ctx: AssemblerParser.LineContext) { }
  override def exitLine(ctx: AssemblerParser.LineContext) {
    values.clear()
  }
  override def enterStatement(ctx: AssemblerParser.StatementContext) { }
  override def exitStatement(ctx: AssemblerParser.StatementContext) { }
  override def enterLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) {
    val label = ctx.ID()
    if (label != null) {
      log.debug(s"${label.getText} := ${symbols("**")}")
      symbols(label.getText) = symbols("**")
    }
  }
  override def exitLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) { }
  override def enterOpCommand(ctx: AssemblerParser.OpCommandContext) { }
  override def exitOpCommand(ctx: AssemblerParser.OpCommandContext) {
    symbols("**") += 2
  }
  override def enterDirectiveCommand(ctx: AssemblerParser.DirectiveCommandContext) { }
  override def exitDirectiveCommand(ctx: AssemblerParser.DirectiveCommandContext) { }
  override def enterOperand(ctx: AssemblerParser.OperandContext) { }
  override def exitOperand(ctx: AssemblerParser.OperandContext) { }
  override def enterImmediateAddr(ctx: AssemblerParser.ImmediateAddrContext) { }
  override def exitImmediateAddr(ctx: AssemblerParser.ImmediateAddrContext) { }
  override def enterDirectAddr(ctx: AssemblerParser.DirectAddrContext) { }
  override def exitDirectAddr(ctx: AssemblerParser.DirectAddrContext) { }
  override def enterIndexedDirectAddr(ctx: AssemblerParser.IndexedDirectAddrContext) { }
  override def exitIndexedDirectAddr(ctx: AssemblerParser.IndexedDirectAddrContext) { }
  override def enterIndirectAddr(ctx: AssemblerParser.IndirectAddrContext) { }
  override def exitIndirectAddr(ctx: AssemblerParser.IndirectAddrContext) { }
  override def enterPreIndexedIndirectAddr(ctx: AssemblerParser.PreIndexedIndirectAddrContext) { }
  override def exitPreIndexedIndirectAddr(ctx: AssemblerParser.PreIndexedIndirectAddrContext) { }
  override def enterPostIndexedIndirectAddr(ctx: AssemblerParser.PostIndexedIndirectAddrContext) { }
  override def exitPostIndexedIndirectAddr(ctx: AssemblerParser.PostIndexedIndirectAddrContext) { }
  override def enterStringArg(ctx: AssemblerParser.StringArgContext) { }
  override def exitStringArg(ctx: AssemblerParser.StringArgContext) { }
  override def enterListArg(ctx: AssemblerParser.ListArgContext) { }
  override def exitListArg(ctx: AssemblerParser.ListArgContext) { }
  override def enterAssignment(ctx: AssemblerParser.AssignmentContext) { }
  override def exitAssignment(ctx: AssemblerParser.AssignmentContext) {
    log.debug(s"${ctx.ID().getText} := ${values(ctx.expr())}")
    symbols(ctx.ID().getText) = values(ctx.expr())
  }
  override def enterDiv(ctx: AssemblerParser.DivContext) { }
  override def exitDiv(ctx: AssemblerParser.DivContext) {
    log.debug(s"${values(ctx.expr(0))} / ${values(ctx.expr(1))}")
    values(ctx) = values(ctx.expr(0)) / values(ctx.expr(1))
  }
  override def enterAdd(ctx: AssemblerParser.AddContext) { }
  override def exitAdd(ctx: AssemblerParser.AddContext) {
    log.debug(s"${values(ctx.expr(0))} + ${values(ctx.expr(1))}")
    values(ctx) = values(ctx.expr(0)) + values(ctx.expr(1))
  }
  override def enterSub(ctx: AssemblerParser.SubContext) { }
  override def exitSub(ctx: AssemblerParser.SubContext) {
    log.debug(s"${values(ctx.expr(0))} - ${values(ctx.expr(1))}")
    values(ctx) = values(ctx.expr(0)) - values(ctx.expr(1))
  }
  override def enterMult(ctx: AssemblerParser.MultContext) { }
  override def exitMult(ctx: AssemblerParser.MultContext) {
    log.debug(s"${values(ctx.expr(0))} * ${values(ctx.expr(1))}")
    values(ctx) = values(ctx.expr(0)) * values(ctx.expr(1))
  }
  override def enterVar(ctx: AssemblerParser.VarContext) { }
  override def exitVar(ctx: AssemblerParser.VarContext) {
    log.debug(s"${ctx.ID().getText} -> ${symbols.getOrElse(ctx.ID().getText, -1)}")
    values(ctx) = symbols.getOrElse(ctx.ID().getText, -1)
  }
  override def enterChar(ctx: AssemblerParser.CharContext) { }
  override def exitChar(ctx: AssemblerParser.CharContext) {
    log.debug(s"Char ${ctx.CHAR().getText} = ${ctx.CHAR().getText.charAt(1).toInt}")
    values(ctx) = ctx.CHAR().getText
  }
  override def enterParens(ctx: AssemblerParser.ParensContext) { }
  override def exitParens(ctx: AssemblerParser.ParensContext) {
    log.debug(s"[${values(ctx.expr())}]")
    values(ctx) = values(ctx.expr())
  }
  override def enterNum(ctx: AssemblerParser.NumContext) { }
  override def exitNum(ctx: AssemblerParser.NumContext) {
    log.debug(s"Num ${values(ctx.getChild(0))}")
    values(ctx) = values(ctx.getChild(0))
  }
  override def enterRem(ctx: AssemblerParser.RemContext) { }
  override def exitRem(ctx: AssemblerParser.RemContext) {
    log.debug(s"${values(ctx.expr(0))} % ${values(ctx.expr(1))}")
    values(ctx) = values(ctx.expr(0)) % values(ctx.expr(1))
  }
  override def enterNumber(ctx: AssemblerParser.NumberContext) { }
  override def exitNumber(ctx: AssemblerParser.NumberContext) {
    log.debug(s"number ${ctx.getChild(0).getText}")
    values(ctx) = ctx.getChild(0).getText
  }
  override def enterEveryRule(ctx: ParserRuleContext) { }
  override def exitEveryRule(ctx: ParserRuleContext) { }
  override def visitTerminal(node: TerminalNode) { }
  override def visitErrorNode(node: ErrorNode) { }
}