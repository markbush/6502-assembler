package org.bushnet

object AddrMode extends Enumeration {
  type AddrMode = Value
  val Absolute,
      AbsoluteIndexedIndirect,
      AbsoluteIndexedX,
      AbsoluteIndexedY,
      AbsoluteIndirect,
      Immediate,
      Implied,
      Relative,
      ZeroPage,
      ZeroPagePreIndexedIndirect,
      ZeroPageIndexedX,
      ZeroPageIndexedY,
      ZeroPageIndirect,
      ZeroPagePostIndexedIndirect = Value
}

object Operation extends Enumeration {
  type Operation = Value
  val ADC  , AND  , ASL  , BBR0 , BBR1 , BBR2 , BBR3 , BBR4 , BBR5 , BBR6,
      BBR7 , BBS0 , BBS1 , BBS2 , BBS3 , BBS4 , BBS5 , BBS6 , BBS7 , BCC,
      BCS  , BEQ  , BIT  , BMI  , BNE  , BPL  , BRA  , BRK  , BVC  , BVS,
      CLC  , CLD  , CLI  , CLV  , CMP  , CPX  , CPY  , DEC  , DEX  , DEY,
      EOR  , INC  , INX  , INY  , JMP  , JSR  , LDA  , LDX  , LDY  , LSR,
      NOP  , ORA  , PHA  , PHP  , PHX  , PHY  , PLA  , PLP  , PLX  , PLY,
      RMB0 , RMB1 , RMB2 , RMB3 , RMB4 , RMB5 , RMB6 , RMB7 , ROL  , ROR,
      RTI  , RTS  , SBC  , SEC  , SED  , SEI  , SMB0 , SMB1 , SMB2 , SMB3,
      SMB4 , SMB5 , SMB6 , SMB7 , STA  , STP  , STX  , STY  , STZ  , TAX,
      TAY  , TRB  , TSB  , TSX  , TXA  , TXS  , TYA  , WAI = Value
  val relativeAddrOps = List(
    BBR0 , BBR1 , BBR2 , BBR3 , BBR4 , BBR5 , BBR6 , BBR7 ,
    BBS0 , BBS1 , BBS2 , BBS3 , BBS4 , BBS5 , BBS6 , BBS7 ,
    BCC  , BCS  , BEQ  , BMI  , BNE  , BPL  , BRA
  )
}

case class Op(opCode:Int, bytes:Int)

object OpCodes {
  import AddrMode._
  import Operation._
  private val operations = Map(
    ADC -> Map(Absolute -> Op(0x00, 2), AbsoluteIndexedX -> Op(0x00, 2), AbsoluteIndexedY -> Op(0x00, 2), AbsoluteIndirect -> Op(0x00, 2), Immediate -> Op(0x00, 1), Implied -> Op(0x00, 0), Relative -> Op(0x00, 1), ZeroPage -> Op(0x00, 1), ZeroPagePreIndexedIndirect -> Op(0x00, 1), ZeroPageIndexedX -> Op(0x00, 1), ZeroPageIndexedY -> Op(0x00, 1), ZeroPageIndirect -> Op(0x00, 1), ZeroPagePostIndexedIndirect -> Op(0x00, 1)),
    BEQ -> Map(Relative -> Op(0xf0, 1)),
    BMI -> Map(Relative -> Op(0x30, 1)),
    BNE -> Map(Relative -> Op(0xd0, 1)),
    BPL -> Map(Relative -> Op(0x10, 1)),
    CLD -> Map(Implied -> Op(0xd8, 0)),
    CLI -> Map(Implied -> Op(0x58, 0)),
    DEX -> Map(Implied -> Op(0xca, 0)),
    DEY -> Map(Implied -> Op(0x88, 0)),
    INC -> Map(ZeroPage -> Op(0xe6, 1)),
    JMP -> Map(Absolute -> Op(0x4c, 2), AbsoluteIndexedIndirect -> Op(0x7c, 2), AbsoluteIndirect -> Op(0x6c, 2)),
    JSR -> Map(Absolute -> Op(0x20, 2)),
    LDA -> Map(Absolute -> Op(0xad, 2), AbsoluteIndexedX -> Op(0xbd, 2), AbsoluteIndexedY -> Op(0xb9, 2), AbsoluteIndirect -> Op(0x00, 2), Immediate -> Op(0xa9, 1), ZeroPage -> Op(0xa5, 1), ZeroPagePreIndexedIndirect -> Op(0xa1, 1), ZeroPageIndexedX -> Op(0xb5, 1), ZeroPageIndexedY -> Op(0x00, 1), ZeroPageIndirect -> Op(0xb2, 1), ZeroPagePostIndexedIndirect -> Op(0xb1, 1)),
    LDX -> Map(Absolute -> Op(0xae, 2), AbsoluteIndexedY -> Op(0xbe, 2), Immediate -> Op(0xa2, 1), ZeroPage -> Op(0xa6, 1), ZeroPageIndexedY -> Op(0xb6, 1)),
    LDY -> Map(Absolute -> Op(0xac, 2), AbsoluteIndexedX -> Op(0xb2, 2), Immediate -> Op(0xa0, 1), ZeroPage -> Op(0xa4, 1), ZeroPageIndexedX -> Op(0xb4, 1)),
    NOP -> Map(Implied -> Op(0xea, 0)),
    PHA -> Map(Implied -> Op(0x48, 0)),
    PLA -> Map(Implied -> Op(0x68, 0)),
    RTS -> Map(Implied -> Op(0x60, 0)),
    SBC -> Map(Absolute -> Op(0xed, 2), AbsoluteIndexedX -> Op(0xfd, 2), AbsoluteIndexedY -> Op(0xf9, 2), Immediate -> Op(0xe9, 1), ZeroPage -> Op(0xe5, 1), ZeroPagePreIndexedIndirect -> Op(0xe1, 1), ZeroPageIndexedX -> Op(0xf5, 1), ZeroPagePostIndexedIndirect -> Op(0xf1, 1)),
    SEC -> Map(Implied -> Op(0x38, 0)),
    SEI -> Map(Implied -> Op(0x78, 0)),
    STA -> Map(Absolute -> Op(0x8d, 2), AbsoluteIndexedX -> Op(0x9d, 2), AbsoluteIndexedY -> Op(0x99, 2), ZeroPage -> Op(0x85, 1), ZeroPagePreIndexedIndirect -> Op(0x81, 1), ZeroPageIndexedX -> Op(0x95, 1), ZeroPageIndirect -> Op(0x92, 1), ZeroPagePostIndexedIndirect -> Op(0x91, 1)),
    TXS -> Map(Implied -> Op(0x9a, 0))
  )
  
  def apply(op:Operation.Value, addrMode:AddrMode.Value) = operations(op)(addrMode)
  def has(op:Operation.Value, addrMode:AddrMode.Value) = operations(op).contains(addrMode)
}