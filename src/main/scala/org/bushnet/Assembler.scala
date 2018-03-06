package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.apache.log4j.Logger

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

object Assembler extends App {
  val log = Logger.getLogger(this.getClass)
  val is = if (args.size > 0) new FileInputStream(args(0)) else System.in
  val outputStream = if (args.size > 1) new FileOutputStream(args(1)) else System.out
  val input = new ANTLRInputStream(is)
  val lexer = new AssemblerLexer(input)
  val tokens = new CommonTokenStream(lexer)
  val parser = new AssemblerParser(tokens)
  val tree = parser.prog()
  log.debug(tree.toStringTree(parser))
  val walker = new ParseTreeWalker()
  val listener = new TwoPassListener()
  walker.walk(listener, tree)
  log.debug(listener.symbols)
  walker.walk(listener, tree)
  log.debug(listener.symbols)
  log.debug(listener.memory.toList.take(200).map(_.toHexString).grouped(10).toList.map(_.mkString(", ")).mkString("\n"))
  if (listener.memory.size > 16374) {
    log.debug(listener.memory.toList.drop(16374).map(_.toHexString).grouped(10).toList.map(_.mkString(", ")).mkString("\n"))
  }
  outputStream.write(listener.memory.map(_.toByte))
}
