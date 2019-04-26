package org.ai2ra.hso.simpic16f84.sim;

/**
 * Represents a single instruction, consisting out of operation code (OPC) and
 * optional arguments.
 */

public class Instruction {

    /**
     * Specifies the type of operation as assembly mnemonic. It represents the OPC
     * in a readable form.
     */

    public enum OperationCode {

        // Byte-Oriented file register operations

        ADDWF, ANDWF, CLRF, CLRW, COMF, DECF, DECFSZ, INCF, INCFSZ, IORWF, MOVF,
        MOVWF, NOP, RLF, RRF, SUBWF, SWAPF, XORWF,

        // Bit-Oriented file register operations

        BCF, BSF, BTFSC, BTFSS,

        // Literal and control operations

        ADDLW, ANDLW, CALL, CLRWDT, GOTO, IORLW, MOVLW, RETFIE, RETLW, RETURN, SLEEP,
        SUBLW, XORLW
    }

    private OperationCode opc;
    private int[] arguments;

    public Instruction(OperationCode opc, int ... arguments) {

        this.opc = opc;
        this.arguments = arguments;
    }

    public OperationCode getOpc() {

        return opc;
    }

    public int[] getArguments() {

        return arguments;
    }
}
