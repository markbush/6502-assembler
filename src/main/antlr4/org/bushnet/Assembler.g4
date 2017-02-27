grammar Assembler;

prog: (line)* ;

line: statement NEWLINE
    | NEWLINE
    ;

statement: labeledCommand
         | assignment
         ;

labeledCommand: ID? command;

command: OPCODE operand?    # OpCommand
       | '.' ID args?       # DirectiveCommand
       ;

operand: immediateAddr
       | directAddr
       | indexedDirectAddr
       | indirectAddr
       | preIndexedIndirectAddr
       | postIndexedIndirectAddr
       ;

immediateAddr: '#' expr ;
directAddr: expr ;
indexedDirectAddr: expr ',' ('X'|'Y') ;
indirectAddr: '(' expr ')' ;
preIndexedIndirectAddr: '(' expr ',' 'X' ')' ;
postIndexedIndirectAddr: '(' expr ')' ',' 'Y' ;

args: STRING                # StringArg
    | expr (',' expr)*      # ListArg
    ;

assignment: ID '=' expr ;

expr: ID                    # Var
    | expr '*' expr         # Mult
    | expr '/' expr         # Div
    | expr '\\' expr        # Rem
    | expr '+' expr         # Add
    | expr '-' expr         # Sub
    | number                # Num
    | CHAR                  # Char
    | '[' expr ']'          # Parens
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
OPCODE: LETTER LETTER LETTER ;
ID: '**' | LETTER (LETTER|DIGIT)* ;
WS: [ \t]+ -> skip ;
fragment LETTER: [a-zA-Z] ;
fragment DIGIT: [0-9] ;
NEWLINE: '\r'? '\n' ;
COMMENT: ';' ~[\r\n]* -> skip ;
