package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class DirectiveTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("single byte should update memory") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "BYTE", "$3C").evaluate(machine)
    machine.peek(startAddress) should be (0x3c)
    machine.pc should be (startAddress+1)
  }
  test("multiple bytes should update memory in consecutive locations") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "BYTE", "$3C,$1F,'A',3").evaluate(machine)
    machine.peek(startAddress) should be (0x3c)
    machine.peek(startAddress+1) should be (0x1f)
    machine.peek(startAddress+2) should be (65)
    machine.peek(startAddress+3) should be (3)
    machine.pc should be (startAddress+4)
  }
  test("string should update memory with each character") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "BYTE", """ "Hello, World" """).evaluate(machine)
    machine.peek(startAddress) should be (72)
    machine.peek(startAddress+1) should be (101)
    machine.peek(startAddress+2) should be (108)
    machine.peek(startAddress+3) should be (108)
    machine.peek(startAddress+6) should be (32)
    machine.peek(startAddress+10) should be (108)
    machine.peek(startAddress+11) should be (100)
    machine.pc should be (startAddress+12)
  }
  test("combined string and bytes should update memory appropriately") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "BYTE", """$3C,"Hello, World",'A'""").evaluate(machine)
    machine.peek(startAddress) should be (0x3c)
    machine.peek(startAddress+1) should be (72)
    machine.peek(startAddress+2) should be (101)
    machine.peek(startAddress+3) should be (108)
    machine.peek(startAddress+4) should be (108)
    machine.peek(startAddress+7) should be (32)
    machine.peek(startAddress+11) should be (108)
    machine.peek(startAddress+12) should be (100)
    machine.peek(startAddress+13) should be (65)
    machine.pc should be (startAddress+14)
  }
  test("labelled byte should update memory and store location for label") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(Some("HERE"), "BYTE", "$3C").evaluate(machine)
    machine.peek(startAddress) should be (0x3c)
    machine.variable("HERE") should be (startAddress)
    machine.pc should be (startAddress+1)
  }
  test("single dbyte should update two memory locations - high byte, low byte") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "DBYTE", "$173C").evaluate(machine)
    machine.peek(startAddress) should be (0x17)
    machine.peek(startAddress+1) should be (0x3c)
    machine.pc should be (startAddress+2)
  }
  test("multiple dbytes should update mutiple blocks of two memory locations - high byte, low byte") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "DBYTE", "$1C1C,$173C,14").evaluate(machine)
    machine.peek(startAddress) should be (0x1c)
    machine.peek(startAddress+1) should be (0x1c)
    machine.peek(startAddress+2) should be (0x17)
    machine.peek(startAddress+3) should be (0x3c)
    machine.peek(startAddress+4) should be (0x00)
    machine.peek(startAddress+5) should be (0x0e)
    machine.pc should be (startAddress+6)
  }
  test("single word should update two memory locations - low byte, high byte") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "WORD", "$173C").evaluate(machine)
    machine.peek(startAddress) should be (0x3c)
    machine.peek(startAddress+1) should be (0x17)
    machine.pc should be (startAddress+2)
  }
  test("multiple words should update mutiple blocks of two memory locations - low byte, high byte") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Directive(None, "WORD", "$1C1C,$173C,14").evaluate(machine)
    machine.peek(startAddress) should be (0x1c)
    machine.peek(startAddress+1) should be (0x1c)
    machine.peek(startAddress+2) should be (0x3c)
    machine.peek(startAddress+3) should be (0x17)
    machine.peek(startAddress+4) should be (0x0e)
    machine.peek(startAddress+5) should be (0x00)
    machine.pc should be (startAddress+6)
  }
  test("specify output byte range") {
    val machine = new Machine
    Directive(None, "OUTPUT", "$0200,$02FF").evaluate(machine)
    machine.bytes should have size 0x100
  }
}
