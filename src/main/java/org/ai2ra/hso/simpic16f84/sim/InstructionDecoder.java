package org.ai2ra.hso.simpic16f84.sim;

/**
 * Instruction decoder is responsible for decoding the next instruction. Basically this
 * just means that the OPC is separated from it's arguments.
 *
 * @author 0x1C1B
 * @version 0.0.1
 */

public class InstructionDecoder {

    private static final int OPERATION_TYPE_MASK;
    private static final int OPERATION_TYPE_OFFSET;

    private static final int JUMP_OPC_MASK;
    private static final int BIT_OPC_MASK;
    private static final int LITERAL_OPC_MASK;
    private static final int BYTE_OPC_MASK;

    private static final int BIT_ADDRESS_MASK;
    private static final int FILE_ADDRESS_MASK;
    private static final int DESTINATION_BIT_MASK;
    private static final int LITERAL_8BIT_MASK;
    private static final int LITERAL_11BIT_MASK;

    private static final int BIT_BITADDR_OFFSET;
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

        BIT_BITADDR_OFFSET = 7;
        DESTINATION_BIT_OFFSET = 7;
    }

    /**
     * Decodes a given instruction by separating OPC and it's arguments. The decoded
     * version is returned in form of an array. Dependent to the kind of OPC, this array also
     * contains optional arguments.
     *
     * @param instruction The instruction that should be decoded
     * @return Returns an array containing the OPC and optional arguments
     */

    public static int[] decode(int instruction) {

        int[] decoded; // Contains OPC and related arguments

        switch((instruction & OPERATION_TYPE_MASK) >> OPERATION_TYPE_OFFSET) {

            case 0b00: { // Byte-Oriented + Control Operations

                // Distinguish between byte-oriented and control operations

                switch(instruction) {

                    case 0b00_0000_0110_0100: // CLRWDT
                    case 0b00_0000_0000_1001: // RETFIE
                    case 0b00_0000_0000_1000: // RETURN
                    case 0b00_0000_0110_0011: { // SLEEP

                        decoded = new int[1];
                        decoded[0] = instruction;

                        break;
                    }
                    default: {

                        if((instruction >> 7) == 0b00_0001_0) { // CLRW

                            decoded = new int[1];
                            decoded[0] = instruction;

                        } else if((instruction >> 7) == 0b00 && (instruction & 0b1_1111) == 0b00) { // NOP

                            decoded = new int[1];
                            decoded[0] = instruction;

                        } else if((instruction >> 7) == 0b11) { // CLRF

                            decoded = new int[2];
                            decoded[0] = instruction & 0b11_1111_1000_0000;
                            decoded[1] = instruction & FILE_ADDRESS_MASK;

                        } else if((instruction >> 7) == 0b01) { // MOVWF

                            decoded = new int[2];
                            decoded[0] = instruction & 0b11_1111_1000_0000;
                            decoded[1] = instruction & FILE_ADDRESS_MASK;

                        } else {

                            decoded = new int[3];
                            decoded[0] = instruction & BYTE_OPC_MASK;
                            decoded[1] = (instruction & DESTINATION_BIT_MASK) >> DESTINATION_BIT_OFFSET;
                            decoded[2] = instruction & FILE_ADDRESS_MASK;
                        }
                    }
                }

                break;
            }
            case 0b01: { // Bit-Oriented Operations

                decoded = new int[3];
                decoded[0] = instruction & BIT_OPC_MASK;
                decoded[1] = (instruction & BIT_ADDRESS_MASK) >> BIT_BITADDR_OFFSET;
                decoded[2] = instruction & FILE_ADDRESS_MASK;

                break;
            }
            case 0b10: { // Jump Operations

                decoded = new int[2];
                decoded[0] = instruction & JUMP_OPC_MASK;
                decoded[1] = instruction & LITERAL_11BIT_MASK;

                break;
            }
            case 0b11: { // Literal Operations

                decoded = new int[2];
                decoded[0] = instruction & LITERAL_OPC_MASK;
                decoded[1] = instruction & LITERAL_8BIT_MASK;

                break;
            }
            default: {

                throw new IllegalStateException("Illegal operation type determined");
            }
        }

        return decoded;
    }
}
