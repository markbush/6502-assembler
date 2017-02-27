package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.apache.log4j.Logger

import java.io.FileInputStream
import java.io.InputStream

object Assembler extends App {
  val log = Logger.getLogger(this.getClass)
  val file = args(0)
  val is = new FileInputStream(file)
  val input = new ANTLRInputStream(is)
  val lexer = new AssemblerLexer(input)
  val tokens = new CommonTokenStream(lexer)
  val parser = new AssemblerParser(tokens)
  val tree = parser.prog()
  log.debug(tree.toStringTree(parser))
  val walker = new ParseTreeWalker()
  val listener = new TwoPassListener()
  walker.walk(listener, tree)
  log.debug(listener.values)
  log.debug(listener.symbols)
}