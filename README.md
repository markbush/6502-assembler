# 6502-assembler

Assembler for 6502 in Scala.

## Buils and Install

Build JAR file with: `sbt assembly`

This will create `target/scala-2.12/6502-assembler-assembly-2.0.0.jar` which you should move to a suitable place.

## Running the Assembler

Run the assembler on your source code using:

```sh
$ java -jar PATH/TO/6502-assembler-assembly-2.0.0.jar <assembler args>
```

You may find it convenient to wrap this in a script like this:

```sh
#!/bin/sh

JAR="PATH/TO/6502-assembler-assembly-2.0.0.jar"

exec java -jar "${JAR}" "$@"
```

The assembler supports the following arguments:

argument | result
---------|---------
-d | Generate debug output
-o binary_filename | generate binary file
-t tape_file_name | generate paper tape format output (for KIM-1)
-h hex_file_name | generate HEX file (for KIM-1 clone)
-b hex_bytes_file | generate a file just containing the hex codes of the bytes (in ASCII)
-m mame_paste_file | generate file suitable for pasting into MAME
-r report_file | generate an assembler report
assembly_source | input source file

The report contains the original source with the addresses and bytes they encode into.  A symbol table dump is included at the end.  All symbols which end up with a value of `$FFFF` will be flagged with an asterisk as this is the default value used when symbols are referenced before being defined, so these could indicate symbols which were never defined in the program.

All of the output file options can be mixed to produce multiple formats in one run.

## Preprocessor

The input source is preprocessed for `include` lines.  These must be on a line of their own and have the form:

```
include "other-file"
```

The `include` is case insensitive and will cause the referenced file to be included at that point and allows you to split the input over multiple files.

Nested includes are supported.

## Directives Supported

The assembler recognises the following assembler directives which must be preceded by a decimal point.

directive | usage
----------|--------
.BYTE | followed by a comma separated list of bytes or strings (which will be treated as a list of bytes based on the ASCII value of each character)
.DBYTE | followed by a comma separated list of double-byte values which will be stored in high-byte/low-byte order
.WORD | followed by a comma separated list of double-byte values which will be stored in low-byte/high-byte order
.OUTPUT | specifies the first and last addresses which will appear in the output file(s)

There **must** be an `.OUTPUT` directive in order to generate bytes in the output files.  This directive allows you to tailor the exact part of the input to produce results for.  This is especially useful when you have mutually dependent components and want to generate separate output files for them since you can have the main source for each component once, then a different control file using `include` directives for each output result.

The `BYTE`, `DBYTE`, and `WORD` directives reserve as much space as needed for their arguments.

## Variables

Variables are not case sensitive and **must** start with a letter and be followed by any number of letters and numbers.

Variables are introduced when they are used as labels (and will have the value of the program counter at that point), are referenced (in which case they will default to a value of `$FFFF`) or are assigned to.

Assignment statements have the form:

```
VAR = VALUE
```

The special variable `**` refers to the current value of the program counter.

## Syntax

Each assembly command **must** appear on a separate line.

Blank lines are ignored and comments begin with a semicolon and then extend to the end of the line.

Labels must end with a colon.

Numerical values can be input in multiple formats.  The format is specified by preceding the number with one of the following characters:

prefix | usage
-------|-------
none | decimal
$ | hex
@ | octal
% | binary

A character surrounded by single quotes will be replaced with the ASCII value of the character.

The following operators are supported:

operator | value
---------|----------------
\+, \-, \*, / | addition, subtraction, multiplication, division of the values either side
\\\\ | the left value modulo the right value
< | the low byte of the following value
\> | the high byte of the following value
\[ \] | square brackets cause the value inside to be evaluated first and can be nested

If a command can take both zero page and absolute addressing, then zero page addressing will be used if the target address in in zero page.

## Example

The following code:

```
ZP1 = $10
ZP2 = $11

** = $0200
.OUTPUT START,END

START:  LDA #'A'          ; load accumulator with 65
        CMP #%01101100
        BEQ FOUND
        STA $2C
FOUND:  LDA #<START
        STA ZP1
        LDA #>START
        STA ZP2
        LDX #[>END + $02] \\ $10
        TXA
        PHA

END = **-1
```

will produce the following report:

```
              ZP1 = $10
              ZP2 = $11

              ** = $0200
0200        .OUTPUT START,END

0200 A9 41    START:  LDA #'A'          ; load accumulator with 65
0202 C9 6C            CMP #%01101100
0204 F0 02            BEQ FOUND
0206 85 2C            STA $2C
0208 A9 00    FOUND:  LDA #<START
020A 85 10            STA ZP1
020C A9 02            LDA #>START
020E 85 11            STA ZP2
0210 A2 02            LDX #[>END + $02] \\ $10
0212 8A               TXA
0213 48               PHA

              END = **-1
END      $ 213
FOUND    $ 208
START    $ 200
ZP1      $  10
ZP2      $  11
```
