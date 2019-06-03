package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.apache.log4j.Logger;

/**
 * Execution unit is responsible for executing literal operations. Originally this class
 * was and logical is part of the executor itself. Because of maintainability reasons
 * it is separated package-private.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see InstructionExecutor
 */

class LiteralExecutionUnit {

    private static final Logger LOGGER;

    private InstructionExecutor executor;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    LiteralExecutionUnit(InstructionExecutor executor) {

        this.executor = executor;
    }

    /**
     * Adds the content of the working register with the given literal (argument)
     * and stores the result inside of the working register. Modifies the <i>DC-FLag</i>,
     * <i>C-FLAG</i> and the <i>Z-FLAG</i> inside of the status
     * register ({@link RamMemory RAM}).
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeADDLW(Instruction instruction) {

        LOGGER.debug(String.format("ADDLW: Adds literal 0x%02X to working register", instruction.getArguments()[0]));

        executor.checkDigitCarryFlag(0xF < (instruction.getArguments()[0] & 0xF) + (executor.getWorkingRegister() & 0xF));

        int result = instruction.getArguments()[0] + executor.getWorkingRegister();

        executor.checkCarryFlag(result);
        executor.checkZeroFlag(result);
        executor.setWorkingRegister((byte) result);
    }

    /**
     * Subtracts the working register from a given literal (argument) and stores
     * result inside of working register. Modifies the <i>DC-FLag</i>,
     * <i>C-FLAG</i> and the <i>Z-FLAG</i> inside of the status
     * register ({@link RamMemory RAM}).
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeSUBLW(Instruction instruction) {

        LOGGER.debug(String.format("SUBLW: Subtracts literal 0x%02X from working register", instruction.getArguments()[0]));

        executor.checkDigitCarryFlag(0xF < (instruction.getArguments()[0] & 0xF) + ((~executor.getWorkingRegister() + 1) & 0xF));

        int result = instruction.getArguments()[0] - executor.getWorkingRegister();

        executor.checkCarryFlag(result);
        executor.checkZeroFlag(result);
        executor.setWorkingRegister((byte) result);
    }

    /**
     * The content of the working Register is AND'ed with the literal. In this case with the instruction arguments.
     * In this case the instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeANDLW(Instruction instruction) {

        LOGGER.debug(String.format("ANDLW: Conjuncts literal 0x%02X with working register", instruction.getArguments()[0]));

        int result = instruction.getArguments()[0] & executor.getWorkingRegister();

        executor.checkZeroFlag(result);
        executor.setWorkingRegister((byte) result);
    }

    /**
     * The literal (here instruction arguments) is loaded into the workingRegister.
     * In this case the instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeMOVLW(Instruction instruction) {

        LOGGER.debug(String.format("MOVLW: Moves literal 0x%02X into working register", instruction.getArguments()[0]));

        executor.setWorkingRegister((byte) instruction.getArguments()[0]);
    }

    /**
     * The content of the workingRegister is OR'ed with the literal (here instructionArguments).
     * The Instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeIORLW(Instruction instruction) {

        LOGGER.debug(String.format("IORLW: Inclusive disjunction of literal 0x%02X with working register", instruction.getArguments()[0]));

        int result = instruction.getArguments()[0] | executor.getWorkingRegister();

        executor.checkZeroFlag(result);
        executor.setWorkingRegister((byte) result);
    }

    /**
     * The contents of the W register are XOR’ed with the eight bit literal 'k'.
     * The result is placed in the W register.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeXORLW(Instruction instruction) {

        LOGGER.debug(String.format("XORLW: Exclusive disjunction of literal 0x%02X with working register", instruction.getArguments()[0]));

        int result = instruction.getArguments()[0] ^ executor.getWorkingRegister();

        executor.checkZeroFlag(result);
        executor.setWorkingRegister((byte) result);
    }

    /**
     * Returns a value from subroutine. Address of next instruction is poped from stack
     * memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeRETLW(Instruction instruction) {

        // Restores address of next instruction from stack memory

        executor.setProgramCounter(executor.stack.pop());
        executor.setWorkingRegister((byte) instruction.getArguments()[0]); // Stores return value

        LOGGER.debug(String.format("RETLW: Return from subroutine to 0x%04X with value 0x%02X", executor.getProgramCounter(), instruction.getArguments()[0]));
    }
}
