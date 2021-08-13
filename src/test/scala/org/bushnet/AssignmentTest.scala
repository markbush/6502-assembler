package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class AssignmentTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("simple assignment should update symbols") {
    val machine = new Machine
    val assignment = Assignment("VAR", "$A")
    assignment.evaluate(machine)
    machine.variable("VAR") should be (10)
  }
  test("expression assignment should update symbols") {
    val machine = new Machine
    val assignment = Assignment("VAR", "$A * 3 + 4")
    assignment.evaluate(machine)
    machine.variable("VAR") should be (34)
  }
  test("assignment from var expression should update symbols") {
    val machine = new Machine
    machine.variablePut("VAR", 0x03)
    val assignment = Assignment("VAR", "VAR * 16")
    assignment.evaluate(machine)
    machine.variable("VAR") should be (0x30)
  }
}
