package org.bushnet

import java.io.InputStream
import java.io.FileInputStream

import scala.io.Source

object InputReader {
  val Include = """(?i)\s*include\s*"([^"]+)"\s*""".r
  def linesFrom(filename:String):List[String] = {
    linesFromFileOnto(filename, Nil).reverse
  }
  def linesFrom(inputStream:InputStream):List[String] = {
    linesFromStreamOnto(inputStream, Nil).reverse
  }
  private def linesFromFileOnto(filename:String, lines:List[String]):List[String] = {
    linesFromStreamOnto(new FileInputStream(filename), lines)
  }
  private def linesFromStreamOnto(inputStream:InputStream, lines:List[String]):List[String] = {
    Source.fromInputStream(inputStream).getLines().foldLeft(lines) { (prevLines, line) =>
      line match {
        case Include(filename) => linesFromFileOnto(filename, prevLines)
        case _ => line :: prevLines
      }
    }
  }
}