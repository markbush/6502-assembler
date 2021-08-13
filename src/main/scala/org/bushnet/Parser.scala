package org.bushnet

import org.slf4j.LoggerFactory
import java.io.PrintWriter

import scala.util.matching.Regex

class ParserException(message:String) extends Exception(message)

class Parser(reportOut:Option[PrintWriter], lines:List[String], machine:Machine) {
  val log = LoggerFactory.getLogger(this.getClass)
  val blankFmt = "             "
  val bytesFmt = "%04x %s      "
  val noArgsFmt = "%04x %02x      "
  val oneArgsFmt = "%04x %02x %02x   "
  val twoArgsFmt = "%04x %02x %02x %02x"
  val EmptyLine = """^\s*$""".r
  val CommentLine = """^\s*;.*$""".r
  val AssignmentLine = """\s*(\*\*|[a-zA-Z][a-zA-Z0-9]*)\s*=\s*([^;]+).*""".r
  val CommandLine = """\s*(([a-zA-Z][a-zA-Z0-9]*):)?\s*\.([a-zA-Z][a-zA-Z0-9]*)\s+([^;]+).*""".r
  val OpLine = """\s*(([a-zA-Z][a-zA-Z0-9]*):)?\s*([a-zA-Z][a-zA-Z0-9]*)\s*([^;]*).*""".r

  def phaseOne():Unit = {
    log.debug("Pass 1")
    parse(false)
  }

  def phaseTwo():Unit = {
    log.debug("Pass 2")
    parse(true)
    machine.allVariables.foreach { variable =>
      log(true, "%-8s $%4x".format(variable, machine.variable(variable)).toUpperCase)
    }
  }

  private def parse(doReport:Boolean):Unit = {
    lines.foreach { line =>
      log.debug(line)
      parse(doReport, line)
    }
  }

  private def parse(doReport:Boolean, line:String):Unit = {
    val pc = machine.pc
    line match {
      case EmptyLine() => // Nothing to do
        log(doReport, "")
      case CommentLine() => // Nothing to do
        log(doReport, s"${blankFmt} ${line}")
      case AssignmentLine(variable, expression) =>
        log(doReport, s"${blankFmt} ${line}")
        Assignment(variable, expression).evaluate(machine)
      case CommandLine(_, label, command, expression) =>
        Directive(Option(label), command, expression).evaluate(machine)
        val newPc = machine.pc
        val bytes = (pc until newPc).map(addr => "%02x".format(machine.peek(addr))).mkString(" ")
        val record = bytesFmt.format(pc, bytes)
        log(doReport, s"${record} ${line}")
      case OpLine(_, label, mnemonic, arg) =>
        Statement(Option(label), mnemonic, arg).evaluate(machine)
        val newPc = machine.pc
        val record = (newPc - pc) match {
          case 1 => noArgsFmt.format(pc, machine.peek(pc))
          case 2 => oneArgsFmt.format(pc, machine.peek(pc), machine.peek(pc+1))
          case 3 => twoArgsFmt.format(pc, machine.peek(pc), machine.peek(pc+1), machine.peek(pc+2))
        }
        log(doReport, s"${record} ${line}")
      case _ => throw new ParserException(s"Unrecognised input: ${line}")
    }
  }

  private def log(doReport:Boolean, message:String):Unit = {
    if (doReport) {
      reportOut.foreach { out =>
        out.println(message)
      }
    }
  }
}
