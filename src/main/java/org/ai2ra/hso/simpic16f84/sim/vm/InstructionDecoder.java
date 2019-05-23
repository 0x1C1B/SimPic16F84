package org.ai2ra.hso.simpic16f84.sim.vm;

/**
 * InstructionDecoder decodes an numeric instruction literal, commonly a short value,
 * to an {@link Instruction} instance. This decoder implementation supports the whole
 * instruction set of the Pic16F84 MCU.
 *
 * @author 0x1C1B
 * @see Instruction
 */

public class InstructionDecoder {

    /**
     * Mask for the general operation group. There exists basically four groups.
     */
    private static final int OPERATION_TYPE_MASK;
    /**
     * Offset for shifting the operation group identifier to right.
     */
    private static final int OPERATION_TYPE_OFFSET;

    /**
     * Specifies the OPC location for kind of jump operation (e.g. CALL).
     */
    private static final int JUMP_OPC_MASK;
    /**
     * Specifies the OPC location for bit oriented operations
     */
    private static final int BIT_OPC_MASK;
    /**
     * Specifies the OPC location for literal oriented operations (e.g. MOVLW).
     */
    private static final int LITERAL_OPC_MASK;
    /**
     * Specifies the OPC location for byte oriented operations
     */
    private static final int BYTE_OPC_MASK;

    private static final int BIT_ADDRESS_MASK;
    private static final int FILE_ADDRESS_MASK;
    private static final int DESTINATION_BIT_MASK;
    private static final int LITERAL_8BIT_MASK;
    private static final int LITERAL_11BIT_MASK;

    private static final int BIT_ADDRESS_OFFSET;
    private static final int DESTINATION_BIT_OFFSET;

    static {

        OPERATION_TYPE_MASK = 0b11_0000_0000_0000; // Mask for detecting operation type
        OPERATION_TYPE_OFFSET = 12; // Offset for shifting/extracting operation type

        // Operation type specific masks for identifying OPC part of instruction

        JUMP_OPC_MASK = 0b11_1000_0000_0000;
        BIT_OPC_MASK = 0b11_1100_0000_0000;
        LITERAL_OPC_MASK = 0b11_1111_0000_0000;
        BYTE_OPC_MASK = 0b11_1111_0000_0000;

        // Masks for identifying OPC related arguments

        BIT_ADDRESS_MASK = 0b00_0011_1000_0000;
        FILE_ADDRESS_MASK = 0b00_0000_0111_1111;
        DESTINATION_BIT_MASK = 0b00_0000_1000_0000;
        LITERAL_8BIT_MASK = 0b00_0000_1111_1111;
        LITERAL_11BIT_MASK = 0b00_0111_1111_1111;

        // Offset for shifting/extracting OPC related arguments

        BIT_ADDRESS_OFFSET = 7;
        DESTINATION_BIT_OFFSET = 7;
    }

    /**
     * Decodes a given instruction by separating OPC and it's arguments. The decoded
     * version is returned in form of an {@link Instruction Instruction Object}.
     * Dependent to the kind of OPC, this instruction also contains optional arguments.
     *
     * @param code The instruction code that should be decoded
     * @return Returns an instruction object containing the OPC and optional arguments
     * @throws IllegalArgumentException Thrown if instruction couldn't be decoded
     * @see Instruction
     */

    public static Instruction decode(int code) throws IllegalArgumentException {

        switch ((code & OPERATION_TYPE_MASK) >> OPERATION_TYPE_OFFSET) {

            case 0b00: { // Byte-Oriented + Control Operations

                return decodeByteOrControlOriented(code);
            }
            case 0b01: { // Bit-Oriented Operations

                return decodeBitOriented(code);
            }
            case 0b10: { // Jump Operations

                return decodeJumpOriented(code);
            }
            case 0b11: { // Literal Operations

                return decodeLiteralOriented(code);
            }
        }

        throw new IllegalArgumentException("Illegal operation type determined");
    }

    /**
     * Decodes a byte/control operation. It requires that only <b>byte/control</b>
     * operations are tried to decode by this method. Otherwise it will fail.
     *
     * @param code The instruction code that should be decoded
     * @return Returns the decoded version
     * @throws IllegalArgumentException Thrown if no such instruction was found
     */

    private static Instruction decodeByteOrControlOriented(int code) throws IllegalArgumentException {

        // Distinguish between byte-oriented and control operations

        switch (code) {

            case 0x0064: { // CLRWDT

                return new Instruction(Instruction.OperationCode.CLRWDT);
            }
            case 0x0009: { // RETFIE

                return new Instruction(Instruction.OperationCode.RETFIE);
            }
            case 0x0008: { // RETURN

                return new Instruction(Instruction.OperationCode.RETURN);
            }
            case 0x0063: { // SLEEP

                return new Instruction(Instruction.OperationCode.SLEEP);
            }
            default: {

                if ((code >> 7) == 0b00_0001_0) { // CLRW

                    return new Instruction(Instruction.OperationCode.CLRW);

                } else if ((code >> 7) == 0b00 && (code & 0b1_1111) == 0b00) { // NOP

                    return new Instruction(Instruction.OperationCode.NOP);

                } else if ((code >> 7) == 0b11) { // CLRF

                    int address = code & FILE_ADDRESS_MASK;
                    return new Instruction(Instruction.OperationCode.CLRF, address);

                } else if ((code >> 7) == 0b01) { // MOVWF

                    int address = code & FILE_ADDRESS_MASK;
                    return new Instruction(Instruction.OperationCode.MOVWF, address);

                } else {

                    int destination = (code & DESTINATION_BIT_MASK) >> DESTINATION_BIT_OFFSET;
                    int address = code & FILE_ADDRESS_MASK;

                    switch (code & BYTE_OPC_MASK) {

                        case 0x0700: { // ADDWF

                            return new Instruction(Instruction.OperationCode.ADDWF, destination, address);
                        }
                        case 0x0500: { // ANDWF

                            return new Instruction(Instruction.OperationCode.ANDWF, destination, address);
                        }
                        case 0x0900: { // COMF

                            return new Instruction(Instruction.OperationCode.COMF, destination, address);
                        }
                        case 0x0300: { // DECF

                            return new Instruction(Instruction.OperationCode.DECF, destination, address);
                        }
                        case 0x0B00: { // DECFSZ

                            return new Instruction(Instruction.OperationCode.DECFSZ, destination, address);
                        }
                        case 0x0A00: { // INCF

                            return new Instruction(Instruction.OperationCode.INCF, destination, address);
                        }
                        case 0x0F00: { // INCFSZ

                            return new Instruction(Instruction.OperationCode.INCFSZ, destination, address);
                        }
                        case 0x0400: { // IORWF

                            return new Instruction(Instruction.OperationCode.IORWF, destination, address);
                        }
                        case 0x0800: { // MOVF

                            return new Instruction(Instruction.OperationCode.MOVF, destination, address);
                        }
                        case 0x0D00: { // RLF

                            return new Instruction(Instruction.OperationCode.RLF, destination, address);
                        }
                        case 0x0C00: { // RRF

                            return new Instruction(Instruction.OperationCode.RRF, destination, address);
                        }
                        case 0x0200: { // SUBWF

                            return new Instruction(Instruction.OperationCode.SUBWF, destination, address);
                        }
                        case 0x0E00: { // SWAPF

                            return new Instruction(Instruction.OperationCode.SWAPF, destination, address);
                        }
                        case 0x0600: { // XORWF

                            return new Instruction(Instruction.OperationCode.XORWF, destination, address);
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("Illegal operation type determined");
    }

    /**
     * Decodes a bit operation. It requires that only <b>bit</b> operations
     * are tried to decode by this method. Otherwise it will fail.
     *
     * @param code The instruction code that should be decoded
     * @return Returns the decoded version
     * @throws IllegalArgumentException Thrown if no such instruction was found
     */

    private static Instruction decodeBitOriented(int code) {

        int bitAddress = (code & BIT_ADDRESS_MASK) >> BIT_ADDRESS_OFFSET;
        int fileAddress = code & FILE_ADDRESS_MASK;

        switch (code & BIT_OPC_MASK) {

            case 0x1000: { // BCF

                return new Instruction(Instruction.OperationCode.BCF, bitAddress, fileAddress);
            }
            case 0x1400: { // BSF

                return new Instruction(Instruction.OperationCode.BSF, bitAddress, fileAddress);
            }
            case 0x1800: { // BTFSC

                return new Instruction(Instruction.OperationCode.BTFSC, bitAddress, fileAddress);
            }
            case 0x1C00: { // BTFSS

                return new Instruction(Instruction.OperationCode.BTFSS, bitAddress, fileAddress);
            }
        }

        throw new IllegalArgumentException("Illegal operation type determined");
    }

    /**
     * Decodes a jump operation. It requires that only <b>jump</b> operations
     * are tried to decode by this method. Otherwise it will fail.
     *
     * @param code The instruction code that should be decoded
     * @return Returns the decoded version
     * @throws IllegalArgumentException Thrown if no such instruction was found
     */

    private static Instruction decodeJumpOriented(int code) {

        int address = code & LITERAL_11BIT_MASK;

        switch (code & JUMP_OPC_MASK) {

            case 0x2000: { // CALL

                return new Instruction(Instruction.OperationCode.CALL, address);
            }
            case 0x2800: { // GOTO

                return new Instruction(Instruction.OperationCode.GOTO, address);
            }
        }

        throw new IllegalArgumentException("Illegal operation type determined");
    }

    /**
     * Decodes a literal operation. It requires that only <b>literal</b> operations
     * are tried to decode by this method. Otherwise it will fail.
     *
     * @param code The instruction code that should be decoded
     * @return Returns the decoded version
     * @throws IllegalArgumentException Thrown if no such instruction was found
     */

    private static Instruction decodeLiteralOriented(int code) throws IllegalArgumentException {

        int literal = code & LITERAL_8BIT_MASK;

        switch (code & LITERAL_OPC_MASK) {

            case 0x3F00: // ADDLW
            case 0x3E00: {

                return new Instruction(Instruction.OperationCode.ADDLW, literal);
            }
            case 0x3900: { // ANDLW

                return new Instruction(Instruction.OperationCode.ANDLW, literal);
            }
            case 0x3800: { // IORLW

                return new Instruction(Instruction.OperationCode.IORLW, literal);
            }
            case 0x3300: // MOVLW
            case 0x3200:
            case 0x3100:
            case 0x3000: {

                return new Instruction(Instruction.OperationCode.MOVLW, literal);
            }
            case 0x3700: // RETLW
            case 0x3600:
            case 0x3500:
            case 0x3400: {

                return new Instruction(Instruction.OperationCode.RETLW, literal);
            }
            case 0x3D00: // SUBLW
            case 0x3C00: {

                return new Instruction(Instruction.OperationCode.SUBLW, literal);
            }
            case 0x3A00: { // XORLW

                return new Instruction(Instruction.OperationCode.XORLW, literal);
            }
        }

        throw new IllegalArgumentException("Illegal operation type determined");
    }
}
