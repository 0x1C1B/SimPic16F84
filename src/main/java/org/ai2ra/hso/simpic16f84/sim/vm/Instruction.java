package org.ai2ra.hso.simpic16f84.sim.vm;

/**
 * Simple pseudo instruction of the Pic16F84. It represents a single
 * machine instruction consisting out of the <i>OPC (Operation Code)</i>,
 * represented by an assembly mnemonic, and optional numeric <i>arguments</i>.
 *
 * @author 0x1C1B
 * @see OperationCode
 */

public class Instruction {

    /**
     * Specifies the type of operation as assembly mnemonic. The contained operation
     * mnemonics are Pic16F84 related and are representing the whole instruction set.
     *
     * @author 0x1C1B
     * @see Instruction
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
    /** Optional arguments depending to the {@link Instruction#opc OPC} */
    private int[] arguments;

    /**
     * Constructs a new instruction consisting out of OPC and optional integer
     * arguments.
     *
     * @param opc The operation code as assembly mnemonic
     * @param arguments Optional numeric arguments
     * @see OperationCode
     */

    public Instruction(OperationCode opc, int ... arguments) {

        this.opc = opc;
        this.arguments = arguments;
    }

    /**
     * The operation code that indicates the kind of operation.
     *
     * @return Returns the operation code
     */

    public OperationCode getOpc() {

        return opc;
    }

    /**
     * Optional arguments of the instruction. Existence and amount depends to the
     * kind of operation.
     *
     * @return Returns the optional numeric arguments
     */

    public int[] getArguments() {

        return arguments;
    }
}
