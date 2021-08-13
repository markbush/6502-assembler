package org.bushnet

import org.slf4j.LoggerFactory

import java.io.OutputStream
import java.io.FileOutputStream

object OutputWriter {
  val log = LoggerFactory.getLogger(this.getClass)

  def writeObject(machine:Machine, outputFilename:String) = {
    val memory = machine.bytes
    val outStream = new FileOutputStream(outputFilename)
    outStream.write(memory.map(_.toByte))
    outStream.close()
  }
  def writeTape(machine:Machine, out:OutputStream) = {
	  log.debug("Tape output")
	  val lineEnd = Array[Byte]('\r', '\n')
	  val lineSize = 24
	  val lineFormat = ";%02x%04x%s%04x"
	  val checkSumF = (checkSum:Int) => checkSum
	  val lastLineF = (numLines:Int)=>";00%04x%04x".format(numLines, numLines)
	  write(machine, lineSize, out, lineFormat, lineEnd, checkSumF, lastLineF)
  }
  def writeHex(machine:Machine, out:OutputStream) = {
	  log.debug("Hex output")
	  val lineEnd = Array[Byte]('\r', '\n')
	  val lineSize = 32
	  val lineFormat = ":%02x%04x00%s%02x"
	  val checkSumF = (checkSum:Int) => 0x100 - ((checkSum) & 0xff)
	  val lastLineF = (x:Int)=>":00000001FF"
	  write(machine, lineSize, out, lineFormat, lineEnd, checkSumF, lastLineF)
  }
  private def write(machine:Machine, lineSize:Int, out:OutputStream,
      lineFormat:String, lineEnd:Array[Byte], checkSumF:Int=>Int, lastLineF:Int=>String) = {
    val memory = machine.bytes
    var lineAddress = machine.startAddress
    val lines = memory.grouped(lineSize).toList
    lines.foreach { line =>
      val bytes = line.map("%02x".format(_)).mkString
      val addrLow = lineAddress & 0xff
      val addrHigh = lineAddress / 0x100
      val checkSum = checkSumF(line.sum + line.size + addrLow + addrHigh)
      val lineOut = lineFormat.format(line.size, lineAddress, bytes, checkSum).toUpperCase
      out.write(lineOut.map(_.toByte).toArray)
      out.write(lineEnd)
      log.debug(lineOut)
      lineAddress += lineSize
    }
	  val lastLine =lastLineF(lines.size).toUpperCase
	  out.write(lastLine.map(_.toByte).toArray)
    out.write(lineEnd)
    log.debug(lastLine)
  }
}
