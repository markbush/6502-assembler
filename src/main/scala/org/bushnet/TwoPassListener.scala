package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import scala.collection.mutable.Map
import scala.collection.convert.WrapAsScala._

class TwoPassListener extends AssemblerBaseListener {
  var memory = new Array(32768)
  var symbols = Map[String,Int]("**" -> 0)
  var memoryStart = 0
  val values = new ParseTreeProperty[Int] {
    def apply(node: ParseTree) = { get(node) }
    def update(node: ParseTree, value: Int) = { put(node, value) }
    override def toString = annotations.keySet().map(key => s"${key.getText} => ${annotations.get(key)}").mkString("{", ", ", "}")
  }
  implicit def stringToInt(s: String) = s.charAt(0) match {
    case '$' => Integer.parseInt(s.tail, 16)
    case '@' => Integer.parseInt(s.tail, 8)
    case '%' => Integer.parseInt(s.tail, 2)
    case _ => s.toInt
  }

  override def enterProg(ctx: AssemblerParser.ProgContext) { }
  override def exitProg(ctx: AssemblerParser.ProgContext) { }
  override def enterLine(ctx: AssemblerParser.LineContext) { }
  override def exitLine(ctx: AssemblerParser.LineContext) {
    symbols("**") += 2
  }
  override def enterStatement(ctx: AssemblerParser.StatementContext) { }
  override def exitStatement(ctx: AssemblerParser.StatementContext) { }
  override def enterLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) { }
  override def exitLabeledCommand(ctx: AssemblerParser.LabeledCommandContext) {
    val label = ctx.ID()
    if (label != null) {
      symbols(label.getText) = symbols("**")
    }
  }
  override def enterOpCommand(ctx: AssemblerParser.OpCommandContext) { }
  override def exitOpCommand(ctx: AssemblerParser.OpCommandContext) { }
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
    val value = ctx.expr().getText
    symbols(ctx.ID().getText) = value
  }
  override def enterDiv(ctx: AssemblerParser.DivContext) { }
  override def exitDiv(ctx: AssemblerParser.DivContext) { }
  override def enterAdd(ctx: AssemblerParser.AddContext) { }
  override def exitAdd(ctx: AssemblerParser.AddContext) { }
  override def enterSub(ctx: AssemblerParser.SubContext) { }
  override def exitSub(ctx: AssemblerParser.SubContext) {
    values(ctx) = values(ctx.expr(0)) - values(ctx.expr(1))
  }
  override def enterMult(ctx: AssemblerParser.MultContext) { }
  override def exitMult(ctx: AssemblerParser.MultContext) { }
  override def enterVar(ctx: AssemblerParser.VarContext) { }
  override def exitVar(ctx: AssemblerParser.VarContext) {
    values(ctx) = symbols.getOrElse(ctx.ID().getText, 0)
  }
  override def enterChar(ctx: AssemblerParser.CharContext) { }
  override def exitChar(ctx: AssemblerParser.CharContext) { }
  override def enterParens(ctx: AssemblerParser.ParensContext) { }
  override def exitParens(ctx: AssemblerParser.ParensContext) { }
  override def enterNum(ctx: AssemblerParser.NumContext) { }
  override def exitNum(ctx: AssemblerParser.NumContext) {
    values(ctx) = ctx.getChild(0).getText
  }
  override def enterRem(ctx: AssemblerParser.RemContext) { }
  override def exitRem(ctx: AssemblerParser.RemContext) { }
  override def enterNumber(ctx: AssemblerParser.NumberContext) { }
  override def exitNumber(ctx: AssemblerParser.NumberContext) {
    values(ctx) = ctx.getChild(0).getText
  }
  override def enterEveryRule(ctx: ParserRuleContext) { }
  override def exitEveryRule(ctx: ParserRuleContext) { }
  override def visitTerminal(node: TerminalNode) { }
  override def visitErrorNode(node: ErrorNode) { }
}