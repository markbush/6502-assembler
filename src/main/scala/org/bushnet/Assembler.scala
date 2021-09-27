package org.bushnet

import org.slf4j.LoggerFactory
import org.slf4j.Logger.ROOT_LOGGER_NAME
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

import scala.io.Source
import scala.collection.mutable.Map

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter

object Assembler extends App {
  val log = LoggerFactory.getLogger(this.getClass)
  val arguments = (new ArgParser).parse(args)
  if (arguments.contains("debug")) {
    val root = LoggerFactory.getLogger(ROOT_LOGGER_NAME).asInstanceOf[Logger]
    root.setLevel(Level.DEBUG)
  }
  log.debug(arguments.toString)

  val inputStream = arguments.get("inputFilename").map(f => new FileInputStream(f)).getOrElse(System.in)
  val reportFilename = arguments.getOrElse("reportFilename", "assembler.rpt")
  val reportStream = new PrintWriter(reportFilename)

  val sourceLines = InputReader.linesFrom(inputStream)

  val machine = new Machine

  val parser = new Parser(Some(reportStream), sourceLines, machine)
  parser.phaseOne()
  parser.phaseTwo()
  reportStream.close()

  arguments.get("tapeFilename").foreach { tapeFilename =>
    val tapeStream = new FileOutputStream(tapeFilename)
    OutputWriter.writeTape(machine, tapeStream)
    tapeStream.close()
  }
  arguments.get("mameFilename").foreach { mameFilename =>
    val mameStream = new FileOutputStream(mameFilename)
    OutputWriter.writeMame(machine, mameStream)
    mameStream.close()
  }
  arguments.get("hexFilename").foreach { hexFilename =>
    val hexStream = new FileOutputStream(hexFilename)
    OutputWriter.writeHex(machine, hexStream)
    hexStream.close()
  }
  arguments.get("hexBytesFilename").foreach { hexBytesFilename =>
    val hexBytesStream = new FileOutputStream(hexBytesFilename)
    OutputWriter.writeHexBytes(machine, hexBytesStream)
    hexBytesStream.close()
  }
  arguments.get("outputFilename").foreach { outputFilename =>
    OutputWriter.writeObject(machine, outputFilename)
  }
}
