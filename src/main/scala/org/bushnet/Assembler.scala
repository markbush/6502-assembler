package org.bushnet

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import java.io.FileInputStream
import java.io.InputStream

object Assembler extends App {
  val file = args(0)
  val is = new FileInputStream(file)
  val input = new ANTLRInputStream(is)
  val lexer = new AssemblerLexer(input)
  val tokens = new CommonTokenStream(lexer)
  val parser = new AssemblerParser(tokens)
  val tree = parser.prog()
  println(tree.toStringTree(parser))
  val walker = new ParseTreeWalker()
  val listener = new TwoPassListener()
  walker.walk(listener, tree)
  println(listener.values)
  println(listener.symbols)
}