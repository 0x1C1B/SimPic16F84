package org.ai2ra.hso.simpic16f84.sim;

/**
 * Instruction is the core class for the whole execution flow. It represents a single
 * machine instruction consisting out of the <i>OPC (Operation Code)</i> and it's
 * numeric <i>arguments</i>.
 *
 * @author 0x1C1B
 */

public class Instruction {

    /**
     * Specifies the type of operation as assembly mnemonic.
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

    /**
     * The operation code which indicates the type of operation
     */
    private OperationCode opc;

    /**
     * Optional arguments depending to the {@link Instruction#opc OPC}
     */
    private int[] arguments;

    /**
     * Constructs a new instruction consisting out of OPC and optional integer
     * arguments.
     *
     * @param opc The operation code as assembly mnemonic
     * @param arguments Optional integer arguments
     * @see OperationCode
     */

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
