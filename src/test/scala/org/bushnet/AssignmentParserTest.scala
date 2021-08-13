package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class AssignmentParserTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("simple assignment should update symbols") {
    val lines = List("VAR = $A")
    val machine = new Machine
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.variable("VAR") should be (10)
  }
  test("expression assignment should update symbols") {
    val lines = List("VAR = $A * 3 + 4 ; more complex expression with comment")
    val machine = new Machine
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.variable("VAR") should be (34)
  }
  test("assignment from var expression should update symbols") {
    val lines = List("VAR = VAR * 16 ; variable expression")
    val machine = new Machine
    machine.variablePut("VAR", 0x03)
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.variable("VAR") should be (0x30)
  }
  test("multiple assignment should update symbols") {
    val lines = List("VAR1 = $A", "VAR2 = <VAR1 * $3B")
    val machine = new Machine
    val parser = new Parser(None, lines, machine)
    parser.phaseTwo()
    machine.variable("VAR1") should be (10)
    machine.variable("VAR2") should be (0x4e)
  }
}
