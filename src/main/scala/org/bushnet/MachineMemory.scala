package org.bushnet

class MachineMemory(size:Int) {
  def this() {
    this(0x10000)
  }
  private val memory = Array.fill(size)(0x00)
  var pc = 0

  def store(byte:Int):Unit = {
    this(pc) = byte
    pc += 1
  }
  def apply(addr:Int):Int = memory(addr)
  def update(addr:Int, byte:Int):Unit = memory(addr) = byte & 0xff

  def slice(from:Int, to:Int):Array[Int] = memory.slice(from, to+1)
}
