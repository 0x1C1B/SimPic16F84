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

class BitExecutionUnit
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
     * Clears the selected bit inside of a filer register.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeBCF(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("BCF: Clears bit %d of value at address 0x%02X in %s", instruction.getArguments()[0], address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        byte mask = (byte) (0x01 << instruction.getArguments()[0]);

        value = (byte) (value & (~mask)); // Clear bit using the mask

        executor.ram.set(bank, address, value);
    }

    /**
     * Sets the selected bit inside of a filer register.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeBSF(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("BSF: Sets bit %d of value at address 0x%02X in %s", instruction.getArguments()[0], address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        byte mask = (byte) (0x01 << instruction.getArguments()[0]);

        value = (byte) (value | mask); // Sets bit using the mask

        executor.ram.set(bank, address, value);
    }

    /**
     * Test if given bit in file register is clear, if yes skip next instruction otherwise
     * execute it.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeBTFSC(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("BTFSC: Test if bit %d of value at address 0x%02X in %s is clear", instruction.getArguments()[0], address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        byte bit = (byte) (0x01 << instruction.getArguments()[0]);

        if (0 == (value & bit)) { // Check if bit is clear

            /*
            Skip the next operation, in general a jump operation as part of loop.
            The hardware would execute a NOP instead of the actual next instruction.
            This software implementation just skips the next instruction.
             */

            executor.setProgramCounter(executor.getProgramCounter() + 1);
        }
    }

    /**
     * Test if given bit in file register is set, if yes skip next instruction otherwise
     * execute it.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeBTFSS(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("BTFSS: Test if bit %d of value at address 0x%02X in %s is set", instruction.getArguments()[0], address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        byte bit = (byte) (0x01 << instruction.getArguments()[0]);

        if (0 != (value & bit)) { // Check if bit is set

            /*
            Skip the next operation, in general a jump operation as part of loop.
            The hardware would execute a NOP instead of the actual next instruction.
            This software implementation just skips the next instruction.
             */

            executor.setProgramCounter(executor.getProgramCounter() + 1);
        }
    }
}
