package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.apache.log4j.Logger;

/**
 * Execution unit is responsible for executing byte/control operations. Originally
 * this class was and logical is part of the executor itself. Because of
 * maintainability reasons it is separated package-private.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see InstructionExecutor
 */

public class ByteAndControlExecutionUnit {

    private static final Logger LOGGER;

    private InstructionExecutor executor;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    ByteAndControlExecutionUnit(InstructionExecutor executor) {

        this.executor = executor;
    }

    /**
     * Adds the content of working register with a value stored inside the given
     * file register address.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeADDWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { // Indirect addressing

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = executor.ram.get(bank, address); // Fetch value from given file register

            LOGGER.debug(String.format("ADDWF: Adds content at address 0x%02X in %s with working register", address, bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + (executor.getWorkingRegister() & 0x00F) > 0xF) {

                executor.setDigitCarryFlag();

            } else {

                executor.clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if (255 > value + executor.getWorkingRegister()) {

                executor.setCarryFlag();

            } else {

                executor.clearCarryFlag();
            }

            // Check for zero result

            if (0 == value + executor.getWorkingRegister()) {

                executor.setZeroFlag();

            } else {

                executor.clearZeroFlag();
            }

            // Check for selected destination

            if (0 == instruction.getArguments()[0]) {

                executor.setWorkingRegister(value + executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, address, value + executor.getWorkingRegister());
            }

        } else { // Direct addressing

            // Fetch value from given file register using direct addressing

            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("ADDWF: Adds content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + (executor.getWorkingRegister() & 0x00F) > 0xF) {

                executor.setDigitCarryFlag();

            } else {

                executor.clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if (255 > value + executor.getWorkingRegister()) {

                executor.setCarryFlag();

            } else {

                executor.clearCarryFlag();
            }

            // Check for zero result

            if (0 == value + executor.getWorkingRegister()) {

                executor.setZeroFlag();

            } else {

                executor.clearZeroFlag();
            }

            // Check for selected destination

            if (0 == instruction.getArguments()[0]) {

                executor.setWorkingRegister(value + executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value + executor.getWorkingRegister());
            }
        }
    }

    /**
     * AND the W register with register 'f'.
     * If 'd' is 0 the result is stored in the W register.
     * If 'd' is 1 the result is stored back in register 'f'.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeANDWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = executor.ram.get(bank, address); // Fetch value from given file register

            LOGGER.debug(String.format("ANDWF: Conjuncts content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value & executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, address, value & executor.getWorkingRegister());

            }

            //Checking for zero result
            if ((value & executor.getWorkingRegister()) == 0) {

                executor.setZeroFlag();

            } else {

                executor.clearZeroFlag();
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = executor.ram.get(bank, instruction.getArguments()[1]); // Fetch value from given file register

            LOGGER.debug(String.format("ANDWF: Conjuncts content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value & executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value & executor.getWorkingRegister());
            }

            //Checking for zero result
            if ((value & executor.getWorkingRegister()) == 0) {

                executor.setZeroFlag();

            } else {

                executor.clearZeroFlag();
            }
        }
    }

    /**
     * Exclusive OR the contents of the W register with register 'f'.
     * If 'd' is 0 the result is stored in the W register.
     * If 'd' is 1 the result is stored back in register 'f'.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeXORWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = executor.ram.get(bank, address);

            LOGGER.debug(String.format("XORWF: Exclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value ^ executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, address, value ^ executor.getWorkingRegister());

            }

            //Checking for zero result
            if ((value ^ executor.getWorkingRegister()) == 0) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("XORWF: Exclusive disjunction of content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value ^ executor.getWorkingRegister());

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value ^ executor.getWorkingRegister());

            }

            //Checking for zero result
            if ((value ^ executor.getWorkingRegister()) == 0) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
        }
    }

    /**
     * Subtract (2’s complement method) W register from register 'f'.
     * If 'd' is 0 the result is stored in the W register.
     * If 'd' is 1 the result is stored back in register 'f'.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    void executeSUBWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = executor.ram.get(bank, address);

            LOGGER.debug(String.format("SUBLW: Subtracts content at address 0x%02X in %s from working register", address, bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + ((~executor.getWorkingRegister() + 1) & 0x000F) > 0xF) {

                executor.setDigitCarryFlag();

            } else {

                executor.clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow
            if (255 > (instruction.getArguments()[0] & 0x00FF) + ((~executor.getWorkingRegister() + 1) & 0x00FF)) {

                executor.setCarryFlag();

            } else {

                executor.clearCarryFlag();
            }

            // Check for zero result.
            if (0 == executor.getWorkingRegister() - value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(executor.getWorkingRegister() - value);

            } else {

                executor.ram.set(bank, address, executor.getWorkingRegister() - value);
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("SUBLW: Subtracts content at address 0x%02X in %s from working register", instruction.getArguments()[1], bank));

            if ((value & 0x000F) + ((~executor.getWorkingRegister() + 1) & 0x000F) > 0xF) {

                executor.setDigitCarryFlag();

            } else {

                executor.clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow
            if (255 > (instruction.getArguments()[0] & 0x00FF) + ((~executor.getWorkingRegister() + 1) & 0x00FF)) {

                executor.setCarryFlag();

            } else {

                executor.clearCarryFlag();
            }

            // Check for zero result.
            if (0 == executor.getWorkingRegister() - value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(executor.getWorkingRegister() - value);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], executor.getWorkingRegister() - value);
            }
        }
    }

    /**
     * Clears the working register by setting current value to zero.
     */

    void executeCLRW() {

        LOGGER.debug("CLRW: Clears the working register");

        executor.setWorkingRegister(0);
        executor.setZeroFlag();
    }

    /**
     * Returns from subroutine. Address of next instruction is poped from stack
     * memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeRETURN(Instruction instruction) {

        /*
        Restores address of next instruction from stack memory
         */

        executor.setProgramCounter(executor.stack.pop());

        LOGGER.debug(String.format("RETURN: Return from subroutine to 0x%04X", executor.getProgramCounter()));
    }

    /**
     * Move data from W register to register 'f'
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */
    void executeMOVWF(Instruction instruction) {

        if (0 == instruction.getArguments()[0]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVWF: Moves data from working register to address 0x%02X in %s", address, bank));

            // Moving data from W register to 'f' register
            executor.ram.set(bank, address, executor.getWorkingRegister());


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVWF: Moves data from working register to address 0x%02X in %s", instruction.getArguments()[0], bank));

            // Moving data from W register to 'f' register
            executor.ram.set(bank, instruction.getArguments()[0], executor.getWorkingRegister());
        }
    }

    /**
     * The contents of register ’f’ are cleared
     * and the Z bit is set.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeCLRF(Instruction instruction) {

        if (0 == instruction.getArguments()[0]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("CLRF: Clears data from register at address 0x%02X in %s", address, bank));

            // Moving data from W register to 'f' register
            executor.ram.set(bank, address, 0);

            // Setting Zero Flag
            executor.setZeroFlag();

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("CLRF: Clears data from register at address 0x%02X in %s", instruction.getArguments()[0], bank));

            // Moving data from W register to 'f' register
            executor.ram.set(bank, instruction.getArguments()[0], 0);

            // Setting Zero Flag
            executor.setZeroFlag();
        }
    }

    /**
     * The contents of register ’f’ are complemented. If ’d’ is 0 the result is stored in
     * W. If ’d’ is 1 the result is stored back in
     * register ’f’.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeCOMF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, address);


            // Checking for Zero result
            if (0 == ~value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }

            LOGGER.debug(String.format("COMF: Complementing data from register at address 0x%02X in %s", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {
                // "Erasing" the upper bits due to the usage of int.
                executor.setWorkingRegister((~value) & 0b1111_1111);

            } else {
                // "Erasing" the upper bits due to the usage of int.
                executor.ram.set(bank, address, ((~value) & 0b1111_1111));
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            // Checking for Zero result
            if (0 == ~value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }

            LOGGER.debug(String.format("COMF: Complementing data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {
                // "Erasing" the upper bits due to the usage of int.
                executor.setWorkingRegister((~value) & 0b1111_1111);

            } else {
                // "Erasing" the upper bits due to the usage of int.
                executor.ram.set(bank, instruction.getArguments()[1], ((~value) & 0b1111_1111));
            }
        }
    }

    /**
     * Decrement register ’f’. If ’d’ is 0 the
     * result is stored in the W register. If ’d’ is
     * 1 the result is stored back in register ’f’.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeDECF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, address);


            LOGGER.debug(String.format("DECF: Decrements data from register at address 0x%02X in %s", address, bank));

            // Checking for Zero result
            if (0 == value - 1) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (0 > value - 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0xFF);

            } else if (0 > value - 1) {

                executor.ram.set(bank, address, 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value - 1);

            } else {

                executor.ram.set(bank, address, value - 1);
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("DECF: Decrements data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Checking for Zero result
            if (0 == value - 1) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (0 > value - 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0xFF);

            } else if (0 > value - 1) {

                executor.ram.set(bank, instruction.getArguments()[1], 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value - 1);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value - 1);
            }
        }
    }

    /**
     * The contents of register ’f’ are incremented. If ’d’ is 0 the result is placed in
     * the W register. If ’d’ is 1 the result is
     * placed back in register ’f’.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeINCF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, address);


            LOGGER.debug(String.format("INCF: Increments data from register at address 0x%02X in %s", address, bank));

            // Checking for Zero result
            if (0 == value + 1) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (255 < value + 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0x00);
                executor.setZeroFlag();

            } else if (255 < value + 1) {

                executor.ram.set(bank, address, 0x00);
                executor.setZeroFlag();
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value + 1);

            } else {

                executor.ram.set(bank, address, value + 1);
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("INCF: Increments data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Checking for Zero result
            if (0 == value + 1) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (255 < value + 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0x00);
                executor.setZeroFlag();

            } else if (255 < value + 1) {

                executor.ram.set(bank, instruction.getArguments()[1], 0x00);
                executor.setZeroFlag();
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value + 1);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value + 1);
            }
        }
    }

    /**
     * The contents of register f is moved to a
     * destination dependant upon the status
     * of d. If d = 0, destination is W register. If
     * d = 1, the destination is file register f
     * itself. d = 1 is useful to test a file register since status flag Z is affected.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeMOVF(Instruction instruction) {
        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVF: Moves data from register at address 0x%02X in %s to Working register or itself", address, bank));

            int value = executor.ram.get(bank, address);

            // Checking for Zero Flag
            if (0 == value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value);

            } else {

                executor.ram.set(bank, address, value);
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVF: Moves data from register at address 0x%02X in %s to Working register or itself", instruction.getArguments()[1], bank));

            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            // Checking for Zero Flag
            if (0 == value) {

                executor.setZeroFlag();
            } else {

                executor.clearZeroFlag();
            }


            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value);
            }
        }
    }

    /**
     * Inclusive OR the W register with register ’f’. If ’d’ is 0 the result is placed in the
     * W register. If ’d’ is 1 the result is placed
     * back in register ’f’.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeIORWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, address);

            byte firstByte = (byte) (value & 0xFF);

            byte firstBits = (byte) ((firstByte & 0x0F) << 4);
            byte lastBits = (byte) ((firstByte & 0xF0) >> 4);

            int returnValue = (firstBits + lastBits);
            LOGGER.debug(String.format("IORWF: Inclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(returnValue);

            } else {

                executor.ram.set(bank, address, returnValue);
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            byte firstByte = (byte) (value & 0xFF);

            byte firstBits = (byte) ((firstByte & 0x0F) << 4);
            byte lastBits = (byte) ((firstByte & 0xF0) >> 4);

            int returnValue = (firstBits + lastBits);

            LOGGER.debug(String.format("IORWF: Inclusive disjunction of content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(returnValue);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], returnValue);
            }
        }
    }


    /**
    * The contents of register ’f’ are decremented. If ’d’ is 0 the result is placed in the
    * W register. If ’d’ is 1 the result is placed
    * back in register ’f’.
    * If the result is not 0, the next instruction, is
    * executed. If the result is 0, then a NOP is
    * executed instead making it a 2T CY instruction.
    * @param instruction Instruction consisting out of OPC and arguments
     */
    void executeDECFSZ(Instruction instruction){

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, address);


            LOGGER.debug(String.format("DECFSZ: Decrements data from register at address 0x%02X in %s", address, bank));

            // Checking for Zero result
            if (0 == value - 1) {

                // NOP is executed.
            } else {

                // The next Instruction is being executed.
            }
            // Checking if value gets negative after decrementing.
            if (0 > value - 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0xFF);

            } else if (0 > value - 1) {

                executor.ram.set(bank, address, 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value - 1);

            } else {

                executor.ram.set(bank, address, value - 1);
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = executor.ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("DECFSZ: Decrements data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Checking for Zero result
            if (0 == value - 1) {

            // NOP is executed.
            } else {

            // The next Instruction is being executed.
            }
            // Checking if value gets negative after decrementing.
            if (0 > value - 1 && instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(0xFF);

            } else if (0 > value - 1) {

                executor.ram.set(bank, instruction.getArguments()[1], 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                executor.setWorkingRegister(value - 1);

            } else {

                executor.ram.set(bank, instruction.getArguments()[1], value - 1);
            }
        }
    }

	 /**
	  * The contents of register ’f’ are incremented. If ’d’ is 0 the result is placed in
	  * the W register. If ’d’ is 1 the result is
	  * placed back in register ’f’.
	  * If the result is not 0, the next instruction is
	  * executed. If the result is 0, a NOP is executed instead making it a 2TCY instruction.
	  * @param instruction Instruction consisting out of OPC and arguments
	  */
	 void executeINCFSZ(Instruction instruction){

		  if (0 == instruction.getArguments()[1]) { //Indirect addressing.

				// Get the lower 7 Bits of FSR if indirect addressing
				int address = executor.ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

				// Determine selected bank
				RamMemory.Bank bank = 0 == executor.getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

				// Fetching value
				int value = executor.ram.get(bank, address);


				LOGGER.debug(String.format("INCFSZ: Increments data from register at address 0x%02X in %s", address, bank));

				// Checking for Zero result
				if (0 == value + 1) {

					 // NOP is executed.
				} else {

					 // The next Instruction is being executed.
				}
				// Checking if value gets negative after decrementing.
				if (255 < value + 1 && instruction.getArguments()[0] == 0) {

					 executor.setWorkingRegister(0x00);
					 executor.setZeroFlag();

				} else if (255 < value + 1) {

					 executor.ram.set(bank, address, 0x00);
					 executor.setZeroFlag();
				}
				//Checking for destination.
				if (instruction.getArguments()[0] == 0) {

					 executor.setWorkingRegister(value + 1);

				} else {

					 executor.ram.set(bank, address, value + 1);
				}


		  } else { //Direct addressing.

				// Determine selected bank
				RamMemory.Bank bank = 0 == executor.getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

				// Fetching value
				int value = executor.ram.get(bank, instruction.getArguments()[1]);

				LOGGER.debug(String.format("INCFSZ: Increments data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

				// Checking for Zero result
				if (0 == value + 1) {

					 // NOP is executed.
				} else {

					 // The next Instruction is being executed.
				}
				// Checking if value gets negative after decrementing.
				if (255 < value + 1 && instruction.getArguments()[0] == 0) {

					 executor.setWorkingRegister(0x00);
					 executor.setZeroFlag();

				} else if (255 < value + 1) {

					 executor.ram.set(bank, instruction.getArguments()[1], 0x00);
					 executor.setZeroFlag();
				}
				//Checking for destination.
				if (instruction.getArguments()[0] == 0) {

					 executor.setWorkingRegister(value + 1);

				} else {

					 executor.ram.set(bank, instruction.getArguments()[1], value + 1);
				}
		  }
	 }
}



