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
