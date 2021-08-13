package org.bushnet

import org.slf4j.LoggerFactory

class InvalidAssignmentException(message:String) extends Exception(message)

object Assignment {
  def apply(variable:String, expr:String) = new Assignment(variable, expr)
}

class Assignment(variable:String, expr:String) {
  val log = LoggerFactory.getLogger(this.getClass)

  def evaluate(machine:Machine):Unit = {
    log.debug(s"ASGN: ${variable} = ${expr}")
    val exprValue = Expression(expr.trim).evaluate(machine)
    machine.variablePut(variable.trim, exprValue)
  }
}
