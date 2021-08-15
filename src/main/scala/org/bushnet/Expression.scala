package org.bushnet

import org.slf4j.LoggerFactory

class InvalidExpressionException(message:String) extends Exception(message)

object Expression {
  def apply(input:String) = new Expression(input)
}

class Expression(input:String) {
  val log = LoggerFactory.getLogger(this.getClass)

  val Variable = """\s*(\*\*|[a-zA-Z][a-zA-Z0-9]*)\s*""".r
  val NumberDecimal = """\s*([0-9]+)\s*""".r
  val NumberHex = """\s*\$([0-9a-fA-F]+)\s*""".r
  val NumberOct = """\s*\@([0-7]+)\s*""".r
  val NumberBin = """\s*\%([01]+)\s*""".r
  val NumberCharacter = """\s*'(.)'?\s*""".r
  val Multiply = """\s*(.+)\s*\*\s*(.+)\s*""".r
  val Divide = """\s*(.+)\s*\/\s*(.+)\s*""".r
  val Remainder = """\s*(.+)\s*\\\\\s*(.+)\s*""".r
  val Add = """\s*(.+)\s*\+\s*(.+)\s*""".r
  val Subtract = """\s*(.+)\s*\-\s*(.+)\s*""".r
  val Parens = """(.*)\s*\[([^]]+)\]\s*(.*)""".r
  val LowByte = """\s*<(.+)""".r
  val HighByte = """\s*>(.+)""".r

  def evaluate(machine:Machine):Int = {
    log.debug(s"EVAL: ${input}")
    input match {
      case LowByte(expr) =>
        val exprValue = Expression(expr.trim).evaluate(machine)
        log.debug(s"<${expr} (${exprValue}) => ${exprValue & 0xff}")
        exprValue & 0xff
      case HighByte(expr) =>
        val exprValue = Expression(expr.trim).evaluate(machine)
        log.debug(s">${expr} (${exprValue}) => ${(exprValue / 0x100) & 0xff}")
        (exprValue / 0x100) & 0xff
      case Variable(varName) =>
        log.debug(s"${varName} => ${"$%x".format(machine.variable(varName))} (${machine.variable(varName)})")
        machine.variable(varName)
      case NumberDecimal(number) => number.toInt
      case NumberHex(number) => Integer.parseInt(number, 16)
      case NumberOct(number) => Integer.parseInt(number, 8)
      case NumberBin(number) => Integer.parseInt(number, 2)
      case NumberCharacter(char) => char(0).toInt
      case Parens(left, expr, right) =>
        val parenValue = Expression(expr.trim).evaluate(machine)
        log.debug(s"${left} [<${expr}> (${parenValue})] ${right}")
        val newExpr = s"${left} ${parenValue} ${right}"
        Expression(newExpr.trim).evaluate(machine)
      case Add(left, right) =>
        val leftValue = Expression(left.trim).evaluate(machine)
        val rightValue = Expression(right.trim).evaluate(machine)
        log.debug(s"${left} (${leftValue}) + ${right} (${rightValue}) => ${leftValue + rightValue}")
        leftValue + rightValue
      case Subtract(left, right) =>
        val leftValue = Expression(left.trim).evaluate(machine)
        val rightValue = Expression(right.trim).evaluate(machine)
        log.debug(s"${left} (${leftValue}) - ${right} (${rightValue}) => ${leftValue - rightValue}")
        leftValue - rightValue
      case Multiply(left, right) =>
        val leftValue = Expression(left.trim).evaluate(machine)
        val rightValue = Expression(right.trim).evaluate(machine)
        log.debug(s"${left} (${leftValue}) * ${right} (${rightValue}) => ${leftValue * rightValue}")
        leftValue * rightValue
      case Divide(left, right) =>
        val leftValue = Expression(left.trim).evaluate(machine)
        val rightValue = Expression(right.trim).evaluate(machine)
        log.debug(s"${left} (${leftValue}) / ${right} (${rightValue}) => ${leftValue / rightValue}")
        leftValue / rightValue
      case Remainder(left, right) =>
        val leftValue = Expression(left.trim).evaluate(machine)
        val rightValue = Expression(right.trim).evaluate(machine)
        log.debug(s"${left} (${leftValue}) \\\\ ${right} (${rightValue}) => ${leftValue % rightValue}")
        leftValue % rightValue
      case _ => throw new InvalidExpressionException(s"Unrecognised expression: ${input}")
    }
  }
}
