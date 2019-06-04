package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.apache.log4j.Logger;

/**
 * Execution unit is responsible for executing jump operations. Originally this class
 * was and logical is part of the executor itself. Because of maintainability reasons
 * it is separated package-private.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see InstructionExecutor
 */

class JumpExecutionUnit {

    private static final Logger LOGGER;

    private InstructionExecutor executor;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    JumpExecutionUnit(InstructionExecutor executor) {

        this.executor = executor;
    }

    /**
     * Push the current address as return point to stack and calls a
     * subroutine. This basically means that it leaves the current control
     * flow by jumping to another address.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeCALL(Instruction instruction) {

        /*
        Save address of next instruction to stack memory
         */

        executor.stack.push(executor.getProgramCounter());

        /*
        Consists out of the opcode/address given as argument and the upper bits
        (bit 3 + 4) of PCLATH register.
         */

        int pclathBits = (executor.ram.get(RamMemory.SFR.PCLATH) & 0b0001_1000) << 8;

        int address = instruction.getArguments()[0]; // Load jump address
        address &= 0b00111_1111_1111; // Clear upper two bits
        address |= pclathBits; // Adding PCLATH

        executor.setProgramCounter(address);

        LOGGER.debug(String.format("CALL: Stores return address 0x%04X and calls subroutine at 0x%04X", executor.stack.top(), executor.getProgramCounter()));
    }

    /**
     * Makes a jump to the given address inside of program memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeGOTO(Instruction instruction) {

        /*
        Consists out of the opcode/address given as argument and the upper bits
        (bit 3 + 4) of PCLATH register.
         */

        int pclathBits = (executor.ram.get(RamMemory.SFR.PCLATH) & 0b0001_1000) << 8;

        int address = instruction.getArguments()[0]; // Load jump address
        address &= 0b00111_1111_1111; // Clear upper two bits
        address |= pclathBits; // Adding PCLATH

        executor.setProgramCounter(address);

        LOGGER.debug(String.format("GOTO: Goes to instruction at 0x%04X", executor.getProgramCounter()));
    }
}
