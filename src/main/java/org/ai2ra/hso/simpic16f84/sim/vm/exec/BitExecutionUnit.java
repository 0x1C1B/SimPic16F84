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
}
