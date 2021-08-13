package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class OpParserTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("simple sequence of operations") {
    val lines = List(
      "START:  CLC",
      "LOOP:   LDA $1740 ; get port A",
      "        BEQ LOOP  ; wait for data",
      "        JSR $1C00",
      "        RTS"
    )
    val machine = new Machine
    val startAddress = 0x200
    machine.pc = startAddress
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.variable("START") should be (startAddress)
    machine.variable("LOOP") should be (startAddress+1)
    machine.peek(startAddress) should be (0x18)    // CLC
    machine.peek(startAddress+1) should be (0xad)  // LDA absolute
    machine.peek(startAddress+2) should be (0x40)
    machine.peek(startAddress+3) should be (0x17)
    machine.peek(startAddress+4) should be (0xf0)  // BEQ
    machine.peek(startAddress+5) should be (0xfb)  // back 5
    machine.peek(startAddress+6) should be (0x20)  // JSR
    machine.peek(startAddress+7) should be (0x00)
    machine.peek(startAddress+8) should be (0x1c)
    machine.peek(startAddress+9) should be (0x60)  // RTS
    machine.pc should be (startAddress+10)
  }
}
