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

            byte firstByte = (byte) (value & 0xFF);

            switch (instruction.getArguments()[0]) {

            case 0: {

                firstByte = (byte) (firstByte & 0b1111_1110);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 1: {

                firstByte = (byte) (firstByte & 0b1111_1101);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 2: {

                firstByte = (byte) (firstByte & 0b1111_1011);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 3: {

                firstByte = (byte) (firstByte & 0b1111_0111);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 4: {

                firstByte = (byte) (firstByte & 0b1110_1111);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 5: {

                firstByte = (byte) (firstByte & 0b1101_1111);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 6: {

                firstByte = (byte) (firstByte & 0b1011_1111);
                value = firstByte;
                executor.ram.set(bank, address, value);
                break;
            }
            case 7: {

                firstByte = (byte) (firstByte & 0b0111_1111);
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

                LOGGER.debug(String.format("ERROR: No bit was found!"));
                break;
            }

            }

        }
    }
}
