package org.bushnet

import org.scalatest.{BeforeAndAfterEach,BeforeAndAfterAll,FunSuite,Matchers}

class ExpressionTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  test("unknown variable should default to 0xffff") {
    val machine = new Machine
    val expression = Expression("unknown")
    expression.evaluate(machine) should be (0xffff)
  }
  test("initial PC should be zero") {
    val machine = new Machine
    val expression = Expression("**")
    expression.evaluate(machine) should be (0x0000)
  }
  test("integer should return itself") {
    val machine = new Machine
    val expression = Expression("14")
    expression.evaluate(machine) should be (14)
  }
  test("hex integer should return itself") {
    val machine = new Machine
    val expression = Expression("$14")
    expression.evaluate(machine) should be (0x14)
  }
  test("oct integer should return itself") {
    val machine = new Machine
    val expression = Expression("@14")
    expression.evaluate(machine) should be (12)
  }
  test("bin integer should return itself") {
    val machine = new Machine
    val expression = Expression("%1110")
    expression.evaluate(machine) should be (14)
  }
  test("multiply numbers should return product") {
    val machine = new Machine
    val expression = Expression("12 * $A")
    expression.evaluate(machine) should be (120)
  }
  test("multiply vars should return product") {
    val machine = new Machine
    machine.variablePut("Num1", 3)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 * Num2")
    expression.evaluate(machine) should be (12)
  }
  test("multiply multiple values should return product") {
    val machine = new Machine
    machine.variablePut("Num1", 3)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 * 5 * Num2")
    expression.evaluate(machine) should be (60)
  }
  test("dividing numbers should return quotient") {
    val machine = new Machine
    val expression = Expression("$A0 / %10")
    expression.evaluate(machine) should be (0x50)
  }
  test("dividing vars should return quotient") {
    val machine = new Machine
    machine.variablePut("Num1", 36)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 / Num2")
    expression.evaluate(machine) should be (9)
  }
  test("dividing multiple values should return quotient") {
    val machine = new Machine
    machine.variablePut("Num1", 36)
    machine.variablePut("Num2", 3)
    val expression = Expression("Num1 / 3 / Num2")
    expression.evaluate(machine) should be (4)
  }
  test("remainder when dividing numbers exactly should return zero") {
    val machine = new Machine
    val expression = Expression("""$A0 \\ %10""")
    expression.evaluate(machine) should be (0)
  }
  test("remainder when dividing numbers should return correct result") {
    val machine = new Machine
    val expression = Expression("""$A0 \\ %11""")
    expression.evaluate(machine) should be (1)
  }
  test("remainder when dividing vars should return correct result") {
    val machine = new Machine
    machine.variablePut("Num1", 166)
    machine.variablePut("Num2", 16)
    val expression = Expression("""Num1 \\ Num2""")
    expression.evaluate(machine) should be (6)
  }
  test("adding numbers should return sum") {
    val machine = new Machine
    val expression = Expression("12 + $A")
    expression.evaluate(machine) should be (22)
  }
  test("adding vars should return sum") {
    val machine = new Machine
    machine.variablePut("Num1", 3)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 + Num2")
    expression.evaluate(machine) should be (7)
  }
  test("adding multiple values should return sum") {
    val machine = new Machine
    machine.variablePut("Num1", 3)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 + 5 + Num2")
    expression.evaluate(machine) should be (12)
  }
  test("subtracting numbers should return difference") {
    val machine = new Machine
    val expression = Expression("12 - $A")
    expression.evaluate(machine) should be (2)
  }
  test("subtracting vars should return difference") {
    val machine = new Machine
    machine.variablePut("Num1", 4)
    machine.variablePut("Num2", 3)
    val expression = Expression("Num1 - Num2")
    expression.evaluate(machine) should be (1)
  }
  test("subtracting multiple values should return difference") {
    val machine = new Machine
    machine.variablePut("Num1", 12)
    machine.variablePut("Num2", 4)
    val expression = Expression("Num1 - 5 - Num2")
    expression.evaluate(machine) should be (3)
  }
  test("character should return itself") {
    val machine = new Machine
    val expression = Expression("\'C\'")
    expression.evaluate(machine) should be (67)
  }
  test("multiple expressions should return correct result") {
    val machine = new Machine
    val expression = Expression("3 * 4 + 2")
    expression.evaluate(machine) should be (14)
  }
  test("multiple expressions should evaluate in correct order") {
    val machine = new Machine
    val expression = Expression("2 + 3 * 4")
    expression.evaluate(machine) should be (14)
  }
  test("parens should evaluate first") {
    val machine = new Machine
    val expression = Expression("[2 + 3] * 4")
    expression.evaluate(machine) should be (20)
  }
  test("nested parens should evaluate first") {
    val machine = new Machine
    val expression = Expression("5 * [[6 * [2 + 3]] / 15]")
    expression.evaluate(machine) should be (10)
  }
  test("low byte should produce correct result") {
    val machine = new Machine
    val expression = Expression("<$ABCD")
    expression.evaluate(machine) should be (0xcd)
  }
  test("low byte with var calculation should produce correct result") {
    val machine = new Machine
    machine.variablePut("VAL", 0xabcd)
    val expression = Expression("<VAL+1")
    expression.evaluate(machine) should be (0xce)
  }
  test("high byte should produce correct result") {
    val machine = new Machine
    val expression = Expression(">$ABCD")
    expression.evaluate(machine) should be (0xab)
  }
  test("high byte with var calculation should produce correct result") {
    val machine = new Machine
    machine.variablePut("VAL", 0xabcd)
    val expression = Expression(">VAL-$A00")
    expression.evaluate(machine) should be (0xa1)
  }
}
