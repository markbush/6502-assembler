package org.bushnet

class Machine(size:Int) {
  def this() {
    this(0x10000)
  }
  var startAddress = 0x0000
  var endAddress = 0xffff
  private var memory = new MachineMemory(size)
  private var symbolTable = new SymbolTable

  def variable(variable:String):Int = {
    if (variable == "**") {
      memory.pc
    } else {
      symbolTable(variable)
    }
  }
  def variablePut(variable:String, value:Int = memory.pc):Unit = {
    if (variable == "**") {
      memory.pc = value
    } else {
      symbolTable(variable) = value
    }
  }
  def peek(addr:Int):Int = memory(addr)
  def poke(addr:Int, byte:Int):Unit = memory(addr) = byte
  def store(byte:Int):Unit = memory.store(byte)
  def pc:Int = memory.pc
  def pc_$eq(addr:Int):Unit = memory.pc = addr
  def bytes = memory.slice(startAddress, endAddress)
  def allVariables = symbolTable.allVariables
}
