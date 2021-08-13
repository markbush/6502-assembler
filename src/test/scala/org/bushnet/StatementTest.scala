package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class StatementTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("ADC with immediate addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "ADC", "#$3C").evaluate(machine)
    machine.peek(startAddress) should be (0x69)
    machine.peek(startAddress+1) should be (0x3c)
    machine.pc should be (startAddress+2)
  }
  test("CMP with pre indexed indirect addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "CMP", "($34,X)").evaluate(machine)
    machine.peek(startAddress) should be (0xc1)
    machine.peek(startAddress+1) should be (0x34)
    machine.pc should be (startAddress+2)
  }
  test("EOR with post indexed indirect addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "EOR", "($A2),Y").evaluate(machine)
    machine.peek(startAddress) should be (0x51)
    machine.peek(startAddress+1) should be (0xa2)
    machine.pc should be (startAddress+2)
  }
  test("JMP with indirect addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "JMP", "($FFFE)").evaluate(machine)
    machine.peek(startAddress) should be (0x6c)
    machine.peek(startAddress+1) should be (0xfe)
    machine.peek(startAddress+2) should be (0xff)
    machine.pc should be (startAddress+3)
  }
  test("LDA with absolute indexed X addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "LDA", "$1700,X").evaluate(machine)
    machine.peek(startAddress) should be (0xbd)
    machine.peek(startAddress+1) should be (0x00)
    machine.peek(startAddress+2) should be (0x17)
    machine.pc should be (startAddress+3)
  }
  test("LDY with zero page indexed X addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "LDY", "$27,X").evaluate(machine)
    machine.peek(startAddress) should be (0xb4)
    machine.peek(startAddress+1) should be (0x27)
    machine.pc should be (startAddress+2)
  }
  test("ORA with absolute indexed Y addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "ORA", "$8025,Y").evaluate(machine)
    machine.peek(startAddress) should be (0x19)
    machine.peek(startAddress+1) should be (0x25)
    machine.peek(startAddress+2) should be (0x80)
    machine.pc should be (startAddress+3)
  }
  test("LDX with zero page indexed Y addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "LDX", "$CA,Y").evaluate(machine)
    machine.peek(startAddress) should be (0xb6)
    machine.peek(startAddress+1) should be (0xca)
    machine.pc should be (startAddress+2)
  }
  test("JSR with absolute addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "JSR", "$0244").evaluate(machine)
    machine.peek(startAddress) should be (0x20)
    machine.peek(startAddress+1) should be (0x44)
    machine.peek(startAddress+2) should be (0x02)
    machine.pc should be (startAddress+3)
  }
  test("SBC with zero page addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "SBC", "$75").evaluate(machine)
    machine.peek(startAddress) should be (0xe5)
    machine.peek(startAddress+1) should be (0x75)
    machine.pc should be (startAddress+2)
  }
  test("BEQ forward with relative addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "BEQ", "$75").evaluate(machine)
    machine.peek(startAddress) should be (0xf0)
    machine.peek(startAddress+1) should be (0x69)
    machine.pc should be (startAddress+2)
  }
  test("BPL backward with relative addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "BPL", "$05").evaluate(machine)
    machine.peek(startAddress) should be (0x10)
    machine.peek(startAddress+1) should be (0xf9)
    machine.pc should be (startAddress+2)
  }
  test("CLC with implied addressing") {
    val machine = new Machine
    val startAddress = 10
    machine.pc = startAddress
    Statement(None, "CLC", "").evaluate(machine)
    machine.peek(startAddress) should be (0x18)
    machine.pc should be (startAddress+1)
  }
}
