package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class DirectiveParserTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("simple byte should update memory") {
    val startAddress = 10
    val lines = List(".BYTE $3C")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x3c)
    machine.pc should be (startAddress+1)
  }
  test("multiple bytes should update memory in consecutive locations") {
    val startAddress = 10
    val lines = List(".BYTE $3C,$1F,'A',3")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x3c)
    machine.peek(startAddress+1) should be (0x1f)
    machine.peek(startAddress+2) should be (65)
    machine.peek(startAddress+3) should be (3)
    machine.pc should be (startAddress+4)
  }
  test("string should update memory with each character") {
    val startAddress = 10
    val lines = List(""".BYTE "Hello, World" """)
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
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
    val startAddress = 10
    val lines = List(""".BYTE $3C,"Hello, World",'A'""")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
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
    val startAddress = 10
    val lines = List("HERE: .BYTE $3C")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x3c)
    machine.variable("HERE") should be (startAddress)
    machine.pc should be (startAddress+1)
  }
  test("single dbyte should update two memory locations - high byte, low byte") {
    val startAddress = 10
    val lines = List(".DBYTE $173C")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x17)
    machine.peek(startAddress+1) should be (0x3c)
    machine.pc should be (startAddress+2)
  }
  test("multiple dbytes should update mutiple blocks of two memory locations - high byte, low byte") {
    val startAddress = 10
    val lines = List(".DBYTE $1C1C,$173C,14")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x1c)
    machine.peek(startAddress+1) should be (0x1c)
    machine.peek(startAddress+2) should be (0x17)
    machine.peek(startAddress+3) should be (0x3c)
    machine.peek(startAddress+4) should be (0x00)
    machine.peek(startAddress+5) should be (0x0e)
    machine.pc should be (startAddress+6)
  }
  test("single word should update two memory locations - low byte, high byte") {
    val startAddress = 10
    val lines = List(".WORD $173C")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x3c)
    machine.peek(startAddress+1) should be (0x17)
    machine.pc should be (startAddress+2)
  }
  test("multiple words should update mutiple blocks of two memory locations - low byte, high byte") {
    val startAddress = 10
    val lines = List(".WORD $1C1C,$173C,14")
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x1c)
    machine.peek(startAddress+1) should be (0x1c)
    machine.peek(startAddress+2) should be (0x3c)
    machine.peek(startAddress+3) should be (0x17)
    machine.peek(startAddress+4) should be (0x0e)
    machine.peek(startAddress+5) should be (0x00)
    machine.pc should be (startAddress+6)
  }
  test("multiple memory fills should update memory and symbols appropriately") {
    val startAddress = 10
    val lines = List(
      """START: .BYTE $81,$9C,%10100111""",
      """STR:   .BYTE "Hello, World",$00 """,
      """DBL:   .WORD $1C00,$1740"""
    )
    val machine = new Machine
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.peek(startAddress) should be (0x81)
    machine.peek(startAddress+1) should be (0x9c)
    machine.peek(startAddress+2) should be (0xa7)
    machine.peek(startAddress+3) should be (72)
    machine.peek(startAddress+4) should be (101)
    machine.peek(startAddress+5) should be (108)
    machine.peek(startAddress+6) should be (108)
    machine.peek(startAddress+7) should be (111)
    machine.peek(startAddress+8) should be (44)
    machine.peek(startAddress+9) should be (32)
    machine.peek(startAddress+10) should be (87)
    machine.peek(startAddress+11) should be (111)
    machine.peek(startAddress+12) should be (114)
    machine.peek(startAddress+13) should be (108)
    machine.peek(startAddress+14) should be (100)
    machine.peek(startAddress+15) should be (0x00)
    machine.peek(startAddress+16) should be (0x00)
    machine.peek(startAddress+17) should be (0x1c)
    machine.peek(startAddress+18) should be (0x40)
    machine.peek(startAddress+19) should be (0x17)
    machine.pc should be (startAddress+20)
  }
}
