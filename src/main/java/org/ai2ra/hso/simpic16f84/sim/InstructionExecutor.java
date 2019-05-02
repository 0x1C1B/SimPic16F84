package org.ai2ra.hso.simpic16f84.sim;

import org.ai2ra.hso.simpic16f84.sim.mem.EepromMemory;
import org.ai2ra.hso.simpic16f84.sim.mem.ProgramMemory;
import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.mem.StackMemory;

public class InstructionExecutor {

    private Integer workingRegister;
    private Integer instructionRegister;
    private Integer programCounter;

    private ProgramMemory<Integer> programMemory;
    private RamMemory<Integer> ram;
    private StackMemory<Integer> stack;
    private EepromMemory<Integer> eeprom;

    public InstructionExecutor(ProgramMemory<Integer> programMemory, RamMemory<Integer> ram,
                               StackMemory<Integer> stack, EepromMemory<Integer> eeprom) {

        this.workingRegister = 0;
        this.instructionRegister = 0;
        this.programCounter = 0;

        this.programMemory = programMemory;
        this.ram = ram;
        this.stack = stack;
        this.eeprom = eeprom;
    }

    /**
     * Loads, decodes and executes the next instruction inside of program memory. Important
     * to note is that just <b>one</b> instruction is executed per method call.
     */

    public void execute() {

        // Fetch current instruction and move PC

        instructionRegister = programMemory.get(programCounter++);

        // Decode current instruction

        Instruction instruction = InstructionDecoder.decode(null == instructionRegister ? 0 : instructionRegister);

        switch(instruction.getOpc()) {

            case ADDLW: {

                executeADDLW(instruction);
                break;
            }
            case ADDWF: {

                executeADDWF(instruction);
                break;
            }
            case SUBLW: {

                executeSUBLW(instruction);
                break;
            }
            case CLRW: {

                executeCLRW();
                break;
            }
            case NOP:
            default: {

                break; // No operation executed
            }
        }
    }

    // Utility methods

    /**
     * Sets the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    private void setDigitCarryFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000010));
    }

    /**
     * Clears the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    private void clearDigitCarryFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111101));
    }

    /**
     * Sets the carry flag inside of status register {@link RamMemory RAM}.
     */

    private void setCarryFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000001));
    }

    /**
     * Clears the carry flag inside of status register {@link RamMemory RAM}.
     */

    private void clearCarryFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111110));
    }

    /**
     * Sets the zero flag inside of status register {@link RamMemory RAM}.
     */

    private void setZeroFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000100));
    }

    /**
     * Clears the zero flag inside of status register {@link RamMemory RAM}.
     */

    private void clearZeroFlag() {

        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111011));
    }

    /**
     * Returns the RP0 bit of the STATUS register. This bit is used for <b>direct</b>
     * addressing, if bit is set (RP0 = 1) second bank is selected, otherwise the first.
     *
     * @return Returns 0 if first bank is selected, otherwise a none 0 value
     */

    private int getRP0Bit() {

        return (ram.get(RamMemory.SFR.STATUS) & 0b0010_0000) >> 5;
    }

    /**
     * Returns the IRP bit of the STATUS register. This bit is used for
     * <b>indirect</b> addressing.
     *
     * @return Returns 0 if first bank is selected, otherwise a none 0 value
     */

    private int getIRPBit() {

        return (ram.get(RamMemory.SFR.STATUS) & 0b1000_0000) >> 7;
    }

    // Instruction execution implementation

    /**
     * Clears the working register by setting current value to zero.
     */

    private void executeCLRW() {

        workingRegister = 0;
    }

    /**
     * Adds the content of the working register with the given literal (argument)
     * and stores the result inside of the working register. Modifies the <i>DC-FLag</i>,
     * <i>C-FLAG</i> and the <i>Z-FLAG</i> inside of the status
     * register ({@link RamMemory RAM}).
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeADDLW(Instruction instruction) {

        // Check if digit carry is occurring

        if((instruction.getArguments()[0] & 0x000F) + (workingRegister & 0x00F) > 0xF) {

            setDigitCarryFlag();

        } else {

            clearDigitCarryFlag();
        }

        // Check for an arithmetic number overflow

        if(255 > instruction.getArguments()[0] + workingRegister) {

            setCarryFlag();

        } else {

            clearCarryFlag();
        }

        workingRegister = (instruction.getArguments()[0] + workingRegister);

        // Check for zero result

        if(0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * Subtracts the working register from a given literal (argument) and stores
     * result inside of working register. Modifies the <i>DC-FLag</i>,
     * <i>C-FLAG</i> and the <i>Z-FLAG</i> inside of the status
     * register ({@link RamMemory RAM}).
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeSUBLW(Instruction instruction) {

        // Check if digit carry is occurring

        if((instruction.getArguments()[0] & 0x000F) + ((~workingRegister + 1) & 0x000F) > 0xF) {

            setDigitCarryFlag();

        } else {

            clearDigitCarryFlag();
        }

        // Check for an arithmetic number overflow

        if(255 > (instruction.getArguments()[0] & 0x00FF) + ((~workingRegister + 1) & 0x00FF)) {

            setCarryFlag();

        } else {

            clearCarryFlag();
        }

        workingRegister = (instruction.getArguments()[0] - workingRegister);

        // Check for zero result

        if(0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * Adds the content of working register with a value stored inside the given
     * file register address.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeADDWF(Instruction instruction) {

        if(0 == instruction.getArguments()[1]) { // Indirect addressing

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            int value = ram.get(bank, address); // Fetch value from given file register

            // Check if digit carry is occurring

            if((value & 0x000F) + (workingRegister & 0x00F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if(255 > value + workingRegister) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result

            if(0 == value + workingRegister) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }

            // Check for selected destination

            if(0 == instruction.getArguments()[0]) {

                workingRegister = value + workingRegister;

            } else {

                ram.set(bank, address, value + workingRegister);
            }

        } else { // Direct addressing

            // Fetch value from given file register using direct addressing

            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = ram.get(bank, instruction.getArguments()[1]);

            // Check if digit carry is occurring

            if((value & 0x000F) + (workingRegister & 0x00F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if(255 > value + workingRegister) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result

            if(0 == value + workingRegister) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }

            // Check for selected destination

            if(0 == instruction.getArguments()[0]) {

                workingRegister = value + workingRegister;

            } else {

                ram.set(bank, instruction.getArguments()[1], value + workingRegister);
            }
        }
    }
}
