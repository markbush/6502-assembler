package org.bushnet

import org.slf4j.LoggerFactory

class InvalidDirectiveException(message:String) extends Exception(message)

object Directive {
  def apply(label:Option[String], command:String, expr:String) = new Directive(label, command, expr)
}

class Directive(label:Option[String], command:String, expr:String) {
  val log = LoggerFactory.getLogger(this.getClass)
  val StringValue = """\s*"([^"]*)"\s*""".r

  def evaluate(machine:Machine):Unit = {
    log.debug(s"CMD: .${command} ${expr}")
    label.foreach { locationName =>
      machine.variablePut(locationName)
      log.debug(s"${locationName} => ${"$%x".format(machine.variable(locationName))} (${machine.variable(locationName)})")
    }
    command.trim.toUpperCase match {
      case "BYTE" =>
        expr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").foreach { item =>
          item match {
            case StringValue(stringCharacters) =>
              stringCharacters.foreach(c => machine.store(c.toInt))
            case _ =>
              val exprValue = Expression(item.trim).evaluate(machine)
              machine.store(exprValue)
          }
        }
      case "DBYTE" =>
        expr.split(",").foreach { item =>
          val exprValue = Expression(item.trim).evaluate(machine)
          machine.store(exprValue/0x100)
          machine.store(exprValue&0xff)
        }
      case "WORD" =>
        expr.split(",").foreach { item =>
          val exprValue = Expression(item.trim).evaluate(machine)
          machine.store(exprValue&0xff)
          machine.store(exprValue/0x100)
        }
      case "OUTPUT" =>
        val Array(startAddressExpr, endAddressExpr) = expr.split(",")
        val startAddress = Expression(startAddressExpr.trim).evaluate(machine)
        val endAddress = Expression(endAddressExpr.trim).evaluate(machine)
        machine.startAddress = startAddress
        machine.endAddress = endAddress
        log.debug(s"OUTPUT ${startAddressExpr} (${startAddress}) => ${endAddressExpr} (${endAddress})")
      case _ => throw new InvalidDirectiveException(s"Unknown directive: ${command}")
    }
  }
}
