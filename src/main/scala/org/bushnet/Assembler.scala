package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object Assembler extends App {
  val log = LoggerFactory.getLogger(this.getClass)
  private def checkTape(tapeArgs:Array[String]) = {
    if (tapeArgs.size > 0 && tapeArgs(0) == "-t") {
      (true, tapeArgs.tail)
    } else {
      (false, tapeArgs)
    }
  }
  private def checkHex(hexArgs:Array[String]) = {
    if (hexArgs.size > 0 && hexArgs(0) == "-h") {
      (true, hexArgs.tail)
    } else {
      (false, hexArgs)
    }
  }
  private def checkInFileStream(inArgs:Array[String]) = {
    if (inArgs.size > 0) {
      (new FileInputStream(inArgs(0)), inArgs.tail)
    } else {
      (System.in, inArgs)
    }
  }
  private def checkOutFileStream(outArgs:Array[String]) = {
    if (outArgs.size > 0) {
      (new FileOutputStream(outArgs(0)), outArgs.tail)
    } else {
      (System.out, outArgs)
    }
  }
  private def writeObject(memory:Array[Int], out:OutputStream) = {
    out.write(memory.map(_.toByte))
  }
  if (System.getProperty("6502.debug") != null) {
    val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
    root.setLevel(Level.DEBUG)
  }
  val (isTape, args1) = checkTape(args)
  val (isHex, args2) = checkHex(args1)
  val (is, args3) = checkInFileStream(args2)
  val (outputStream, args4) = checkOutFileStream(args3)
  val input = new ANTLRInputStream(is)
  val lexer = new AssemblerLexer(input)
  val tokens = new CommonTokenStream(lexer)
  val parser = new AssemblerParser(tokens)
  val tree = parser.prog()
  log.debug(tree.toStringTree(parser))
  val walker = new ParseTreeWalker()
  val listener = new TwoPassListener()
  walker.walk(listener, tree)
  log.debug(listener.symbols.toString)
  walker.walk(listener, tree)
  log.debug(listener.symbols.toString)
  log.debug(listener.memory.toList.take(200).map(_.toHexString).grouped(10).toList.map(_.mkString(", ")).mkString("\n"))
  if (listener.memory.size > 16374) {
    log.debug(listener.memory.toList.drop(16374).map(_.toHexString).grouped(10).toList.map(_.mkString(", ")).mkString("\n"))
  }
  if (isTape) {
    OutputWriter.writeTape(listener.memory, listener.memoryStart, outputStream)
  } else if (isHex) {
    OutputWriter.writeHex(listener.memory, listener.memoryStart, outputStream)
  } else {
    writeObject(listener.memory, outputStream)
  }
}
