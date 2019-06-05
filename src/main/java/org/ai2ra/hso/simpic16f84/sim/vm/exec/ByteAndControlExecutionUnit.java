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

class ByteAndControlExecutionUnit {

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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("ADDWF: Adds content at address 0x%02X in %s with working register", address, bank));

        /*
        Arithmetic operation is processed with unsigned integers for allow
        checking the carry flag. The byte type cast later will make it signed again.
         */

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = (0xFF & value) + (0xFF & executor.getWorkingRegister());

        executor.checkDigitCarryFlag(0xF < (value & 0xF) + (executor.getWorkingRegister() & 0xF));
        executor.checkCarryFlag(result);
        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("ANDWF: Conjuncts content at address 0x%02X in %s with working register", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = value & executor.getWorkingRegister();

        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("XORWF: Exclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = value ^ executor.getWorkingRegister();

        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("SUBLW: Subtracts content at address 0x%02X in %s from working register", address, bank));

        /*
        Arithmetic operation is processed with unsigned integers for allow
        checking the carry flag. The byte type cast later will make it signed again.
         */

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = (0xFF & value) + (0xFF & (~executor.getWorkingRegister() + 1));

        executor.checkDigitCarryFlag(0xF < (value & 0xF) + ((~executor.getWorkingRegister() + 1) & 0xF));
        executor.checkCarryFlag(result);
        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
        }
    }

    /**
     * Clears the working register by setting current value to zero.
     */

    void executeCLRW() {

        LOGGER.debug("CLRW: Clears the working register");

        executor.setWorkingRegister((byte) 0x00);
        executor.setZeroFlag();
    }

    /**
     * Returns from subroutine. Address of next instruction is poped from stack
     * memory.
     */

    void executeRETURN() {

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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("MOVWF: Moves data from working register to address 0x%02X in %s", address, bank));

        // Moving data from W register to 'f' register
        executor.ram.set(bank, address, executor.getWorkingRegister());
    }

    /**
     * The contents of register ’f’ are cleared
     * and the Z bit is set.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeCLRF(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("CLRF: Clears data from register at address 0x%02X in %s", address, bank));

        // Moving data from W register to 'f' register
        executor.ram.set(bank, address, (byte) 0x00);

        // Setting Zero Flag
        executor.setZeroFlag();
    }

    /**
     * The contents of register ’f’ are complemented. If ’d’ is 0 the result is stored in
     * W. If ’d’ is 1 the result is stored back in
     * register ’f’.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeCOMF(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("COMF: Complementing data from register at address 0x%02X in %s", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = ~value;

        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("DECF: Decrements data from register at address 0x%02X in %s", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = value - 1;

        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("INCF: Increments data from register at address 0x%02X in %s", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = value + 1;

        executor.checkZeroFlag(result);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("MOVF: Moves data from register at address 0x%02X in %s to Working register or itself", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register

        executor.checkZeroFlag(value);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister(value);

        } else {

            executor.ram.set(bank, address, value);
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("IORWF: Inclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register


        executor.checkZeroFlag(executor.getWorkingRegister() | value);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) (executor.getWorkingRegister() | value));

        } else {

            executor.ram.set(bank, address, (byte) (executor.getWorkingRegister() | value));
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

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("DECFSZ: Decrements data from register at address 0x%02X in %s", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register
        int result = value - 1;

        // Checking for Zero result
        if (0 == result) {

            /*
            Skip the next operation, in general a jump operation as part of loop.
            The hardware would execute a NOP instead of the actual next instruction.
            This software implementation just skips the next instruction.
             */

            executor.setProgramCounter(executor.getProgramCounter() + 1);
        }

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister((byte) result);

        } else {

            executor.ram.set(bank, address, (byte) result);
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

         int address = executor.getFileAddress(instruction);
         RamMemory.Bank bank = executor.getSelectedBank(instruction);

         LOGGER.debug(String.format("INCFSZ: Increments data from register at address 0x%02X in %s", address, bank));

         byte value = executor.ram.get(bank, address); // Fetch value from given file register
         int result = value + 1;

         // Checking for Zero result
         if (0 == result) {

            /*
            Skip the next operation, in general a jump operation as part of loop.
            The hardware would execute a NOP instead of the actual next instruction.
            This software implementation just skips the next instruction.
             */

             executor.setProgramCounter(executor.getProgramCounter() + 1);
         }

         // Check for selected destination

         if (0 == instruction.getArguments()[0]) {

             executor.setWorkingRegister((byte) result);

         } else {

             executor.ram.set(bank, address, (byte) result);
         }
	 }

	 /**
	  * The contents of register ’f’ are rotated
	  * one bit to the left through the Carry
	  * Flag. If ’d’ is 0 the result is placed in the
	  * W register. If ’d’ is 1 the result is placed
	  * back in register ’f’.
      *
	  * @param instruction Instruction consisting out of OPC and arguments
	  */

	 void executeRLF (Instruction instruction){

         int address = executor.getFileAddress(instruction);
         RamMemory.Bank bank = executor.getSelectedBank(instruction);

         LOGGER.debug(String.format("RLF: The contents of the register at 0x%02X in %s are rotated one bit to the left through the Carry Flag.", address, bank));

         byte value = executor.ram.get(bank, address); // Fetch value from given file register
         int newCarryFlag = (value & 0b1000_0000) >> 7;

         value = (byte) (value & 0b0111_1111);
         value = (byte) (value << 1);

         if (executor.isCarryFlag()) {

             value |= 0x01; // Carry flag is set value one is used as first bit

         } else {

             value &= (~0x01); // Carry flag isn't set value zero is used as first bit
         }

         // Apply new carry flag value
         if (0x01 == newCarryFlag) {

             executor.setCarryFlag();

         } else {

             executor.clearCarryFlag();
         }

         // Check for selected destination

         if (0 == instruction.getArguments()[0]) {

             executor.setWorkingRegister(value);

         } else {

             executor.ram.set(bank, address, value);
         }
	 }

	 /**
	  * The contents of register ’f’ are rotated
	  * one bit to the right through the Carry
	  * Flag. If ’d’ is 0 the result is placed in the
	  * W register. If ’d’ is 1 the result is placed
	  * back in register ’f’
      *
	  * @param instruction Instruction consisting out of OPC and arguments
	  */

     void executeRRF(Instruction instruction) {

         int address = executor.getFileAddress(instruction);
         RamMemory.Bank bank = executor.getSelectedBank(instruction);

         LOGGER.debug(String.format("RRF: The contents of the register at 0x%02X in %s are rotated one bit to the right through the Carry Flag.", address, bank));

         byte value = executor.ram.get(bank, address); // Fetch value from given file register
         int newCarryFlag = value & 0b0000_0001;

         value = (byte) (value >> 1);

         if (executor.isCarryFlag()) {

             value |= 0x01 << 7; // Carry flag is set value one is used as first bit

         } else {

             value &= ~(0x01 << 7); // Carry flag isn't set value zero is used as first bit
         }

         // Apply new carry flag value
         if (0x01 == newCarryFlag) {

             executor.setCarryFlag();

         } else {

             executor.clearCarryFlag();
         }

         // Check for selected destination

         if (0 == instruction.getArguments()[0]) {

             executor.setWorkingRegister(value);

         } else {

             executor.ram.set(bank, address, value);
         }
	 }

    /**
     * Execute the No Operation instruction. This method exists for the sake of completeness
     * to prevent breaking the runtime counter;
     */

    void executeNOP() {

        LOGGER.debug("NOP: No operation was executed"); // No operation is executed
    }

    /**
     * Exchanges the upper and lower nibbles of the selected file register.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeSWAPF(Instruction instruction) {

        int address = executor.getFileAddress(instruction);
        RamMemory.Bank bank = executor.getSelectedBank(instruction);

        LOGGER.debug(String.format("SWAPF: Exchanges the upper and lower nibbles of register at 0x%02X in %s.", address, bank));

        byte value = executor.ram.get(bank, address); // Fetch value from given file register

        byte lowerNibbles = (byte) ((value & 0x0F) << 4);
        byte upperNibbles = (byte) ((value & 0xF0) >> 4);
        byte result = (byte) (lowerNibbles | upperNibbles);

        // Check for selected destination

        if (0 == instruction.getArguments()[0]) {

            executor.setWorkingRegister(result);

        } else {

            executor.ram.set(bank, address, result);
        }
    }

    /**
     * Returns from a interrupt service routine to the next regular instruction.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    void executeRETFIE(Instruction instruction) {

        // Disables the Global Interrupt Enable (GIE) bit before leaving the ISR

        executor.ram.set(RamMemory.SFR.INTCON, (byte) (executor.ram.get(RamMemory.SFR.INTCON) | 0b1000_0000));

        // Restores address of next instruction from stack memory

        executor.setProgramCounter(executor.stack.pop());

        LOGGER.debug(String.format("RETFIE: Return from Interrupt Service Handler to 0x%04X", executor.getProgramCounter()));
    }
}



