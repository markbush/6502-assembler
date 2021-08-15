package org.bushnet

import org.slf4j.LoggerFactory

class InvalidStatementException(message:String) extends Exception(message)

object Statement {
  def apply(label:Option[String], command:String, expr:String) = new Statement(label, command, expr)
}

class Statement(label:Option[String], command:String, expr:String) {
  val log = LoggerFactory.getLogger(this.getClass)
  val Immediate = """\s*#\s*([^;]+).*""".r                           // #xx
  val PreIndexedIndirectAddr = """\s*\(\s*(.+)\s*,\s*X\s*\).*""".r   // (xx,X)
  val PostIndexedIndirectAddr = """\s*\(\s*(.+)\s*\)\s*,\s*Y.*""".r  // (xx),Y
  val IndirectAddr = """\s*\(\s*(.+)\s*\).*""".r                     // (xx)
  val IndexedDirectAddrX = """\s*(.+)\s*,\s*X.*""".r                 // xx,X
  val IndexedDirectAddrY = """\s*(.+)\s*,\s*Y.*""".r                 // xx,Y
  val DirectAddr = """\s*(.+)""".r                                   // xx

  def evaluate(machine:Machine):Unit = {
    log.debug(s"OP: ${command} ${expr}")
    label.foreach { locationName =>
      machine.variablePut(locationName)
      log.debug(s"${locationName} => ${"$%x".format(machine.variable(locationName))} (${machine.variable(locationName)})")
    }
    val op = Operation.withName(command.trim.toUpperCase)
    val (addrMode, exprValue) = addrModeAndValue(op, machine)
    val opCode = OpCodes(op, addrMode)
    machine.store(opCode.opCode)
    if (opCode.bytes > 0) {
      machine.store(exprValue)
      if (opCode.bytes > 1) {
        machine.store(exprValue/0x100)
      }
    }
  }

  private def addrModeAndValue(op:Operation.Value, machine:Machine):(AddrMode.Value,Int) = {
    expr match {
      case Immediate(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        (AddrMode.Immediate, exprValue)
      case PreIndexedIndirectAddr(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        (AddrMode.ZeroPagePreIndexedIndirect, exprValue)
      case PostIndexedIndirectAddr(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        (AddrMode.ZeroPagePostIndexedIndirect, exprValue)
      case IndirectAddr(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        (AddrMode.AbsoluteIndirect, exprValue)
      case IndexedDirectAddrX(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        var addrMode = if (exprValue < 0x100 && OpCodes.has(op, AddrMode.ZeroPageIndexedX)) {
          AddrMode.ZeroPageIndexedX
        } else {
          AddrMode.AbsoluteIndexedX
        }
        (addrMode, exprValue)
      case IndexedDirectAddrY(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        var addrMode = if (exprValue < 0x100 && OpCodes.has(op, AddrMode.ZeroPageIndexedY)) {
          AddrMode.ZeroPageIndexedY
        } else {
          AddrMode.AbsoluteIndexedY
        }
        (addrMode, exprValue)
      case DirectAddr(value) =>
        val exprValue = Expression(value.trim).evaluate(machine)
        if (Operation.relativeAddrOps.contains(op)) {
          val currentAddr = machine.pc
          val relAddr = exprValue - (currentAddr + 2)
          val target = if (relAddr >= 0) relAddr else relAddr+0x100
          (AddrMode.Relative, target)
        } else if (exprValue < 0x100 && OpCodes.has(op, AddrMode.ZeroPage)) {
          (AddrMode.ZeroPage, exprValue)
        } else {
          (AddrMode.Absolute, exprValue)
        }
      case _ =>
        (AddrMode.Implied, 0)
    }
  }
}
