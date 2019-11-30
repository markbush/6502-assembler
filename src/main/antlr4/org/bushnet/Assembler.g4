grammar Assembler;

prog: (line)* ;

line: statement NEWLINE
    | NEWLINE
    ;

statement: labeledCommand
         | assignment
         ;

labeledCommand: ID? command;

command: DIRECTIVE args?     # DirectiveCommand
       | OPCODE operand?     # OpCommand
       ;

operand: immediateAddr
       | directAddr
       | indexedDirectAddrX
       | indexedDirectAddrY
       | indirectAddr
       | preIndexedIndirectAddr
       | postIndexedIndirectAddr
       ;

immediateAddr: '#' expr ;
directAddr: expr ;
indexedDirectAddrX: expr ',' 'X' ;
indexedDirectAddrY: expr ',' 'Y' ;
indirectAddr: '(' expr ')' ;
preIndexedIndirectAddr: '(' expr ',' 'X' ')' ;
postIndexedIndirectAddr: '(' expr ')' ',' 'Y' ;

args: STRING                # StringArg
    | expr (',' expr)*      # ListArg
    ;

assignment: ID '=' expr     # VarAssign
          | '**' '=' expr   # PcAssign
          ;

expr: ID                    # Var
    | '**'                  # Pc
    | expr '*' expr         # Mult
    | expr '/' expr         # Div
    | expr '\\' expr        # Rem
    | expr '+' expr         # Add
    | expr '-' expr         # Sub
    | number                # Num
    | CHAR                  # Char
    | '[' expr ']'          # Parens
    | '<' expr              # LowByte
    | '>' expr              # HighByte
    ;

number: HEX
      | OCT
      | BIN
      | INT
      ;

STRING: '"' ~["]* '"' ;
INT: DIGIT+ ;
HEX: '$' [0-9a-fA-F]+ ;
OCT: '@' [0-7]+ ;
BIN: '%' [01]+ ;
CHAR: '\'' . ;
OPCODE: ADC  | AND  | ASL  | BBR0 | BBR1 | BBR2 | BBR3 | BBR4 | BBR5 | BBR6
      | BBR7 | BBS0 | BBS1 | BBS2 | BBS3 | BBS4 | BBS5 | BBS6 | BBS7 | BCC 
      | BCS  | BEQ  | BIT  | BMI  | BNE  | BPL  | BRA  | BRK  | BVC  | BVS
      | CLC  | CLD  | CLI  | CLV  | CMP  | CPX  | CPY  | DEC  | DEX  | DEY
      | EOR  | INC  | INX  | INY  | JMP  | JSR  | LDA  | LDX  | LDY  | LSR
      | NOP  | ORA  | PHA  | PHP  | PHX  | PHY  | PLA  | PLP  | PLX  | PLY
      | RMB0 | RMB1 | RMB2 | RMB3 | RMB4 | RMB5 | RMB6 | RMB7 | ROL  | ROR
      | RTI  | RTS  | SBC  | SEC  | SED  | SEI  | SMB0 | SMB1 | SMB2 | SMB3
      | SMB4 | SMB5 | SMB6 | SMB7 | STA  | STP  | STX  | STY  | STZ  | TAX
      | TAY  | TRB  | TSB  | TSX  | TXA  | TXS  | TYA  | WAI ;
DIRECTIVE: '.' ID ;
ID: LETTER (LETTER|DIGIT)* ;
WS: [ \t]+ -> skip ;
fragment LETTER: [a-zA-Z] ;
fragment DIGIT: [0-9] ;
NEWLINE: '\r'? '\n' ;
COMMENT: ';' ~[\r\n]* -> skip ;

fragment ADC: 'ADC' ;
fragment AND: 'AND' ;
fragment ASL: 'ASL' ;
fragment BBR0: 'BBR0' ;
fragment BBR1: 'BBR1' ;
fragment BBR2: 'BBR2' ;
fragment BBR3: 'BBR3' ;
fragment BBR4: 'BBR4' ;
fragment BBR5: 'BBR5' ;
fragment BBR6: 'BBR6' ;
fragment BBR7: 'BBR7' ;
fragment BBS0: 'BBS0' ;
fragment BBS1: 'BBS1' ;
fragment BBS2: 'BBS2' ;
fragment BBS3: 'BBS3' ;
fragment BBS4: 'BBS4' ;
fragment BBS5: 'BBS5' ;
fragment BBS6: 'BBS6' ;
fragment BBS7: 'BBS7' ;
fragment BCC: 'BCC' ;
fragment BCS: 'BCS' ;
fragment BEQ: 'BEQ' ;
fragment BIT: 'BIT' ;
fragment BMI: 'BMI' ;
fragment BNE: 'BNE' ;
fragment BPL: 'BPL' ;
fragment BRA: 'BRA' ;
fragment BRK: 'BRK' ;
fragment BVC: 'BVC' ;
fragment BVS: 'BVS' ;
fragment CLC: 'CLC' ;
fragment CLD: 'CLD' ;
fragment CLI: 'CLI' ;
fragment CLV: 'CLV' ;
fragment CMP: 'CMP' ;
fragment CPX: 'CPX' ;
fragment CPY: 'CPY' ;
fragment DEC: 'DEC' ;
fragment DEX: 'DEX' ;
fragment DEY: 'DEY' ;
fragment EOR: 'EOR' ;
fragment INC: 'INC' ;
fragment INX: 'INX' ;
fragment INY: 'INY' ;
fragment JMP: 'JMP' ;
fragment JSR: 'JSR' ;
fragment LDA: 'LDA' ;
fragment LDX: 'LDX' ;
fragment LDY: 'LDY' ;
fragment LSR: 'LSR' ;
fragment NOP: 'NOP' ;
fragment ORA: 'ORA' ;
fragment PHA: 'PHA' ;
fragment PHP: 'PHP' ;
fragment PHX: 'PHX' ;
fragment PHY: 'PHY' ;
fragment PLA: 'PLA' ;
fragment PLP: 'PLP' ;
fragment PLX: 'PLX' ;
fragment PLY: 'PLY' ;
fragment RMB0: 'RMB0' ;
fragment RMB1: 'RMB1' ;
fragment RMB2: 'RMB2' ;
fragment RMB3: 'RMB3' ;
fragment RMB4: 'RMB4' ;
fragment RMB5: 'RMB5' ;
fragment RMB6: 'RMB6' ;
fragment RMB7: 'RMB7' ;
fragment ROL: 'ROL' ;
fragment ROR: 'ROR' ;
fragment RTI: 'RTI' ;
fragment RTS: 'RTS' ;
fragment SBC: 'SBC' ;
fragment SEC: 'SEC' ;
fragment SED: 'SED' ;
fragment SEI: 'SEI' ;
fragment SMB0: 'SMB0' ;
fragment SMB1: 'SMB1' ;
fragment SMB2: 'SMB2' ;
fragment SMB3: 'SMB3' ;
fragment SMB4: 'SMB4' ;
fragment SMB5: 'SMB5' ;
fragment SMB6: 'SMB6' ;
fragment SMB7: 'SMB7' ;
fragment STA: 'STA' ;
fragment STP: 'STP' ;
fragment STX: 'STX' ;
fragment STY: 'STY' ;
fragment STZ: 'STZ' ;
fragment TAX: 'TAX' ;
fragment TAY: 'TAY' ;
fragment TRB: 'TRB' ;
fragment TSB: 'TSB' ;
fragment TSX: 'TSX' ;
fragment TXA: 'TXA' ;
fragment TXS: 'TXS' ;
fragment TYA: 'TYA' ;
fragment WAI: 'WAI' ;
