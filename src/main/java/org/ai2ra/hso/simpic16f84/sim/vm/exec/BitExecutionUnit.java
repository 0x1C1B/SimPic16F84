package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.apache.log4j.Logger;

/**
 * Execution unit is responsible for executing bit operations. Originally this class
 * was and logical is part of the executor itself. Because of maintainability reasons
 * it is separated package-private.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see InstructionExecutor
 */

public class BitExecutionUnit
{

    private static final Logger LOGGER;

    private InstructionExecutor executor;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    BitExecutionUnit(InstructionExecutor executor)
    {

        this.executor = executor;
    }

    /**
     * Bit ’b’ in register ’f’ is cleared
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */
    void executeBCF(Instruction instruction)
    {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BCF: Bit %s at address 0x%02X is cleared",
                  instruction.getArguments()[0], address));

            int value = executor.ram.get(bank, address);

            int firstByte = value & 0xFF;

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = firstByte & 0b1111_1110;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 1: {

                firstByte = firstByte & 0b1111_1101;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 2: {

                firstByte = firstByte & 0b1111_1011;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 3: {

                firstByte = firstByte & 0b1111_0111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 4: {

                firstByte = firstByte & 0b1110_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 5: {

                firstByte = firstByte & 0b1101_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 6: {

                firstByte = firstByte & 0b1011_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 7: {

                firstByte = firstByte & 0b0111_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            default: {

                LOGGER.debug(String.format("ERROR: No bit was found!"));
                break;
            }

            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BCF: Bit %s at address 0x%02X is cleared",
                  instruction.getArguments()[0], instruction.getArguments()[1]));

            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            int firstByte = value & 0xFF;

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = firstByte & 0b1111_1110;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 1: {

                firstByte = firstByte & 0b1111_1101;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 2: {

                firstByte = firstByte & 0b1111_1011;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 3: {

                firstByte = firstByte & 0b1111_0111;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 4: {

                firstByte = firstByte & 0b1110_1111;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 5: {

                firstByte = firstByte & 0b1101_1111;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 6: {

                firstByte = firstByte & 0b1011_1111;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 7: {

                firstByte = firstByte & 0b0111_1111;
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            default: {

                LOGGER.debug(String.format("ERROR: No bit was found!"));
                break;
            }

            }
        }
    }

    /**
     * Bit ’b’ in register ’f’ is set.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */
    void executeBSF(Instruction instruction)
    {
        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BSF: Bit %s at address 0x%02X is set",
                  instruction.getArguments()[0], address));

            int value = executor.ram.get(bank, address);

            int firstByte = value & 0xFF;

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = firstByte | 0b1111_1110;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 1: {

                firstByte = firstByte | 0b1111_1101;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 2: {

                firstByte = firstByte | 0b1111_1011;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 3: {

                firstByte = firstByte | 0b1111_0111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 4: {

                firstByte = firstByte | 0b1110_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 5: {

                firstByte = firstByte | 0b1101_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 6: {

                firstByte = firstByte | 0b1011_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 7: {

                firstByte = firstByte | 0b0111_1111;
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            default: {

                LOGGER.error(String.format("ERROR: No bit was found!"));
                break;
            }

            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BSF: Bit %s at address 0x%02X is set",
                  instruction.getArguments()[0], instruction.getArguments()[1]));

            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            byte firstByte = (byte) (value & 0xFF);

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = (byte) (firstByte & 0b1111_1110);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 1: {

                firstByte = (byte) (firstByte & 0b1111_1101);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 2: {

                firstByte = (byte) (firstByte & 0b1111_1011);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 3: {

                firstByte = (byte) (firstByte & 0b1111_0111);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 4: {

                firstByte = (byte) (firstByte & 0b1110_1111);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 5: {

                firstByte = (byte) (firstByte & 0b1101_1111);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 6: {

                firstByte = (byte) (firstByte & 0b1011_1111);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            case 7: {

                firstByte = (byte) (firstByte & 0b0111_1111);
                value = firstByte;
                executor.ram.set(bank, instruction.getArguments()[1], value);
                break;
            }
            default: {

                LOGGER.error(String.format("No bit was found!"));
                break;
            }
            }
        }
    }

    /**
     * If bit ’b’ in register ’f’ is ’1’ then the next
     * instruction is executed.
     * If bit ’b’, in register ’f’, is ’0’ then the next
     * instruction is discarded, and a NOP is
     * executed instead, making this a 2TCY
     * instruction.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */
    void executeBTFSC(Instruction instruction)
    {
        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BTFSC: If bit %s at address 0x%02X is set, then the next instruction is executed, else NOP is executed instead",
                  instruction.getArguments()[0], address));

            int value = executor.ram.get(bank, address);

            int firstByte = value & 0xFF;

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = firstByte & 0b0000_0001;

                if (firstByte == 0b0000_0001) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 1: {

                firstByte = firstByte & 0b0000_0010;

                if (firstByte == 0b0000_0010) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 2: {

                firstByte = firstByte & 0b0000_0100;

                if (firstByte == 0b0000_0100) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 3: {

                firstByte = firstByte & 0b0000_1000;

                if (firstByte == 0b0000_1000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 4: {

                firstByte = firstByte & 0b0001_0000;

                if (firstByte == 0b0001_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 5: {

                firstByte = firstByte & 0b0010_0000;

                if (firstByte == 0b0010_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 6: {

                firstByte = firstByte & 0b0100_0000;

                if (firstByte == 0b0100_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 7: {

                firstByte = firstByte & 0b1000_0000;

                if (firstByte == 0b1000_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            default: {

                LOGGER.error(String.format("ERROR: No bit was found!"));
                break;
            }

            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ?
                  RamMemory.Bank.BANK_0 :
                  RamMemory.Bank.BANK_1;

            LOGGER.debug(String.format("BTFSC: If bit %s at address 0x%02X is set, then the next instruction is executed, else NOP is executed instead",
                  instruction.getArguments()[0], instruction.getArguments()[1]));

            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            int firstByte = value & 0xFF;

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = firstByte & 0b0000_0001;

                if (firstByte == 0b0000_0001) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 1: {

                firstByte = firstByte & 0b0000_0010;

                if (firstByte == 0b0000_0010) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 2: {

                firstByte = firstByte & 0b0000_0100;

                if (firstByte == 0b0000_0100) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 3: {

                firstByte = firstByte & 0b0000_1000;

                if (firstByte == 0b0000_1000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 4: {

                firstByte = firstByte & 0b0001_0000;

                if (firstByte == 0b0001_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 5: {

                firstByte = firstByte & 0b0010_0000;

                if (firstByte == 0b0010_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 6: {

                firstByte = firstByte & 0b0100_0000;

                if (firstByte == 0b0100_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            case 7: {

                firstByte = firstByte & 0b1000_0000;

                if (firstByte == 0b1000_0000) {

                    // Next Instruction is executed.

                } else {

                    // NOP operation is executed.

                }

                break;
            }
            default: {

                LOGGER.error(String.format("ERROR: No bit was found!"));
                break;
            }

            }
        }
    }
}
