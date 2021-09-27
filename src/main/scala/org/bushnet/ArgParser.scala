package org.bushnet

import org.slf4j.LoggerFactory

class ArgParseException(message:String) extends Exception(message)

class ArgParser {
  val log = LoggerFactory.getLogger(this.getClass)

  val usage = """Usage: 6502 [-d] [-t tape_out_file] [-h hex_out_file] [-b hex_bytes_out_file] [-o binary_out_file] [-r report_out_file] [input_file]"""

  def parse(args:Array[String]):Map[String,String] = {
    parse(args.toList, Map())
  }

  private def parse(args:List[String], acc:Map[String,String]):Map[String,String] = {
    args match {
      case Nil =>
        acc
      case "-d" :: rest =>
        parse(rest, acc + ("debug" -> "true"))
      case "-m" :: mameFilename :: rest =>
        parse(rest, acc + ("mameFilename" -> mameFilename))
      case "-t" :: tapeFilename :: rest =>
        parse(rest, acc + ("tapeFilename" -> tapeFilename))
      case "-h" :: hexFilename :: rest =>
        parse(rest, acc + ("hexFilename" -> hexFilename))
      case "-b" :: hexBytesFilename :: rest =>
        parse(rest, acc + ("hexBytesFilename" -> hexBytesFilename))
      case "-o" :: outputFilename :: rest =>
        parse(rest, acc + ("outputFilename" -> outputFilename))
      case "-r" :: reportFilename :: rest =>
        parse(rest, acc + ("reportFilename" -> reportFilename))
      case inputFilename :: Nil if (inputFilename.size > 0 && inputFilename(0) != '-') =>
        acc + ("inputFilename" -> inputFilename)
      case _ =>
        log.error(usage)
        System.exit(1)
        Map()
    }
  }
}
