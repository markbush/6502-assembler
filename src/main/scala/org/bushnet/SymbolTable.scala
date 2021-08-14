package org.bushnet

import scala.collection.mutable.Map

class SymbolTable {
  private val symbols = Map[String,Int]()

  def apply(variable:String):Int = symbols.getOrElseUpdate(variable, 0xffff)
  def update(variable:String, value:Int):Unit = symbols(variable) = value

  def allVariables = symbols.keys.toList.sorted
}
