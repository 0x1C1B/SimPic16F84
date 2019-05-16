package org.ai2ra.hso.simpic16f84.sim;

import org.ai2ra.hso.simpic16f84.sim.mem.*;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * InstructionExecutor represents the ALU (Arithmetic Logical Unit) and parts of the
 * CPU (Central Processing Unit) in one class. It's responsible for executing single
 * instructions specified with their OPC. Thereby it's the core of the execution flow
 * and controllable using the {@link InstructionExecutor#execute()} method.
 *
 * @author Freddy1096
 * @author 0x1C1B
 * @see InstructionDecoder
 */

public class InstructionExecutor {

    private static final Logger LOGGER;

    /**
     * Working register used as accumulator.
     */
    private Integer workingRegister;
    /**
     * Contains the next instruction before it's execution.
     */
    private Integer instructionRegister;
    /** The instruction pointer that points to the next instruction in program memory. */
    private Integer programCounter;

    private ProgramMemory<Integer> programMemory;
    private RamMemory<Integer> ram;
    private StackMemory<Integer> stack;
    private EepromMemory<Integer> eeprom;

    /** Used for synchronizing the execution flow. */
    private ReentrantLock lock;
    private PropertyChangeSupport changes;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    /**
     * Constructs a new execution unit by injecting required memory dependencies.
     *
     * @param programMemory The program memory which contains the executable program
     * @param ram The RAM consisting out of SFR's and GPR's
     * @param stack The stack memory, should contain at least eight levels
     * @param eeprom The EEPROM for persisting data beyond restarts
     */

    public InstructionExecutor(ProgramMemory<Integer> programMemory, RamMemory<Integer> ram,
                               StackMemory<Integer> stack, EepromMemory<Integer> eeprom) {

        this.instructionRegister = 0;
        this.programCounter = 0;

        this.programMemory = programMemory;
        this.ram = ram;
        this.stack = stack;
        this.eeprom = eeprom;

        lock = new ReentrantLock();
        changes = new PropertyChangeSupport(this);

        setWorkingRegister(0);
    }

    /**
     * Loads, decodes and executes the next instruction inside of program memory. Important
     * to note is that just <b>one</b> instruction is executed per method call. A typical
     * execution cycle includes the following:
     *
     * <ol>
     *     <li>
     *         Load next instruction, indicated by the {@link InstructionExecutor#programCounter}
     *         into {@link InstructionExecutor#instructionRegister}
     *     </li>
     *     <li>
     *         Increments the {@link InstructionExecutor#programCounter} for pointing
     *         to the next instruction.
     *     </li>
     *     <li>
     *         Decodes the content of the {@link InstructionExecutor#instructionRegister}.
     *     </li>
     *     <li>
     *         Executes the loaded instruction. Internally the instruction implementation
     *         in form of a separate method is called.
     *     </li>
     * </ol>
     *
     * @return Returns the address of the next instruction
     * @see InstructionDecoder
     */

    public int execute() {

        lock.lock();

        try {

            LOGGER.info(String.format("Load OPC from 0x%04X into instruction register (IR)", programCounter));

            // Fetch current instruction and move PC

            instructionRegister = programMemory.get(programCounter++);

            // Decode current instruction

            Instruction instruction = InstructionDecoder.decode(null == instructionRegister ? 0 : instructionRegister);

            switch (instruction.getOpc()) {

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
                case ANDLW: {

                    executeANDLW(instruction);
                    break;
                }
                case MOVLW: {

                    executeMOVLW(instruction);
                    break;
                }
                case ANDWF: {

                    executeANDWF(instruction);
                    break;
                }
                case IORLW: {

                    executeIORLW(instruction);
                    break;
                }
                case XORLW: {

                    executeXORLW(instruction);
                    break;
                }
                case XORWF: {

                    executeXORWF(instruction);
                    break;
                }
                case SUBWF: {

                    executeSUBWF(instruction);
                    break;
                }
                case CALL: {

                    executeCALL(instruction);
                    break;
                }
                case RETURN: {

                    executeRETURN(instruction);
                    break;
                }
                case RETLW: {

                    executeRETLW(instruction);
                    break;
                }
                case GOTO: {

                    executeGOTO(instruction);
                    break;
                }
                case MOVWF: {

                    executeMOVWF(instruction);
                    break;
                }
                case CLRF: {

                    executeCLRF(instruction);
                    break;
                }
					 case COMF: {

					 	 executeCOMF(instruction);
					 	 break;
					 }
                case DECF: {

                    executeDECF(instruction);
                    break;
                }
                case INCF: {

                    executeINCF(instruction);
                    break;
                }
                case MOVF: {

                    executeMOVF(instruction);
                    break;
                }
                case IORWF: {

                    executeIORWF(instruction);
                    break;
                }
                case NOP:
                default: {

                    LOGGER.debug("NOP: No operation was executed");
                    break; // No operation executed
                }
            }

        } catch (MemoryIndexOutOfBoundsException exc) {

            LOGGER.error("Unimplemented address accessed", exc);

        } catch (UnsupportedOperationException exc) {

            LOGGER.error("Unsupported operation code found", exc);

        } finally {

            lock.unlock();
        }

        return programCounter;
    }

    /**
     * Resets status of RAM and working register to the power-on state. The power-on
     * state is defined inside of the data sheet.
     */

    public void reset() {

        LOGGER.info("Reset registers to power-on state");

        workingRegister = 0x00;
        programCounter = 0x00;

        // Initialize the special function registers

        ram.set(RamMemory.SFR.INDF, 0x00);
        ram.set(RamMemory.SFR.TMR0, 0x00);
        ram.set(RamMemory.SFR.PCL, 0x00);
        ram.set(RamMemory.SFR.STATUS, 0b0001_1100);
        ram.set(RamMemory.SFR.FSR, 0x000);
        ram.set(RamMemory.SFR.PORTA, 0x00);
        ram.set(RamMemory.SFR.PORTB, 0x00);
        ram.set(RamMemory.SFR.EEDATA, 0x00);
        ram.set(RamMemory.SFR.EEADR, 0x00);
        ram.set(RamMemory.SFR.PCLATH, 0x00);
        ram.set(RamMemory.SFR.INTCON, 0x00);
        ram.set(RamMemory.SFR.OPTION, 0b1111_1111);
        ram.set(RamMemory.SFR.TRISA, 0b0001_1111);
        ram.set(RamMemory.SFR.TRISB, 0b1111_1111);
        ram.set(RamMemory.SFR.EECON1, 0x00);
        ram.set(RamMemory.SFR.EECON2, 0x00);
    }
    /**
     * Adds a change listener <b>only</b> for observing the executor's state. This pattern
     * is specially intended to use for the working register.
     *
     * @param listener The listener that should be registered
     */

    public void addPropertyChangeListener(PropertyChangeListener listener) {

        changes.addPropertyChangeListener(listener);
    }

    /**
     * Removes a change listener <b>only</b> from the change support of the executor.
     *
     * @param listener The listener that should be removed
     */

    public void removePropertyChangeListener(PropertyChangeListener listener) {

        changes.removePropertyChangeListener(listener);
    }

    // Utility methods

    /**
     * Used for changing content of working register. Moreover this method allows
     * notifying all observers.
     *
     * @param value The value that should be written to working register
     */

    private void setWorkingRegister(Integer value) {

        changes.firePropertyChange("workingRegister", workingRegister, value);
        workingRegister = value;
    }

    /**
     * Used for fetching the content of the working register.
     *
     * @return Returns the current content of the working register
     */

    public Integer getWorkingRegister() {

        return workingRegister;
    }

    /**
     * Sets the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    private void setDigitCarryFlag() {

        LOGGER.info("Set 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000010));
    }

    /**
     * Clears the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    private void clearDigitCarryFlag() {

        LOGGER.info("Clear 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111101));
    }

    /**
     * Sets the carry flag inside of status register {@link RamMemory RAM}.
     */

    private void setCarryFlag() {

        LOGGER.info("Set 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000001));
    }

    /**
     * Clears the carry flag inside of status register {@link RamMemory RAM}.
     */

    private void clearCarryFlag() {

        LOGGER.info("Clear 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111110));
    }

    /**
     * Sets the zero flag inside of status register {@link RamMemory RAM}.
     */

    private void setZeroFlag() {

        LOGGER.info("Set 'Zero' (Z) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000100));
    }

    /**
     * Clears the zero flag inside of status register {@link RamMemory RAM}.
     */

    private void clearZeroFlag() {

        LOGGER.info("Clear 'Zero' (Z) flag inside of STATUS register");
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

        LOGGER.debug("CLRW: Clears the working register");

        setWorkingRegister(0);
        setZeroFlag();
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

        LOGGER.debug(String.format("ADDLW: Adds literal 0x%02X to working register", instruction.getArguments()[0]));

        // Check if digit carry is occurring

        if ((instruction.getArguments()[0] & 0x000F) + (workingRegister & 0x00F) > 0xF) {

            setDigitCarryFlag();

        } else {

            clearDigitCarryFlag();
        }

        // Check for an arithmetic number overflow

        if (255 > instruction.getArguments()[0] + workingRegister) {

            setCarryFlag();

        } else {

            clearCarryFlag();
        }

        setWorkingRegister(instruction.getArguments()[0] + workingRegister);

        // Check for zero result

        if (0 == workingRegister) {

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

        LOGGER.debug(String.format("SUBLW: Subtracts literal 0x%02X from working register", instruction.getArguments()[0]));

        // Check if digit carry is occurring

        if ((instruction.getArguments()[0] & 0x000F) + ((~workingRegister + 1) & 0x000F) > 0xF) {

            setDigitCarryFlag();

        } else {

            clearDigitCarryFlag();
        }

        // Check for an arithmetic number overflow

        if (255 > (instruction.getArguments()[0] & 0x00FF) + ((~workingRegister + 1) & 0x00FF)) {

            setCarryFlag();

        } else {

            clearCarryFlag();
        }

        setWorkingRegister(instruction.getArguments()[0] - workingRegister);

        // Check for zero result

        if (0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * The content of the working Register is AND'ed with the literal. In this case with the instruction arguments.
     * In this case the instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeANDLW(Instruction instruction) {

        LOGGER.debug(String.format("ANDLW: Conjuncts literal 0x%02X with working register", instruction.getArguments()[0]));

        setWorkingRegister(instruction.getArguments()[0] & workingRegister);

        //Checking for a Zero result after the AND operation.

        if (0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * The literal (here instruction arguments) is loaded into the workingRegister.
     * In this case the instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeMOVLW(Instruction instruction) {

        LOGGER.debug(String.format("MOVLW: Moves literal 0x%02X into working register", instruction.getArguments()[0]));

        setWorkingRegister(instruction.getArguments()[0]);
    }

    /**
     * Adds the content of working register with a value stored inside the given
     * file register address.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeADDWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { // Indirect addressing

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = ram.get(bank, address); // Fetch value from given file register

            LOGGER.debug(String.format("ADDWF: Adds content at address 0x%02X in %s with working register", address, bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + (workingRegister & 0x00F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if (255 > value + workingRegister) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result

            if (0 == value + workingRegister) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }

            // Check for selected destination

            if (0 == instruction.getArguments()[0]) {

                setWorkingRegister(value + workingRegister);

            } else {

                ram.set(bank, address, value + workingRegister);
            }

        } else { // Direct addressing

            // Fetch value from given file register using direct addressing

            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("ADDWF: Adds content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + (workingRegister & 0x00F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow

            if (255 > value + workingRegister) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result

            if (0 == value + workingRegister) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }

            // Check for selected destination

            if (0 == instruction.getArguments()[0]) {

                setWorkingRegister(value + workingRegister);

            } else {

                ram.set(bank, instruction.getArguments()[1], value + workingRegister);
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

    private void executeANDWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = ram.get(bank, address); // Fetch value from given file register

            LOGGER.debug(String.format("ANDWF: Conjuncts content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value & workingRegister);

            } else {

                ram.set(bank, address, value & workingRegister);

            }

            //Checking for zero result
            if ((value & workingRegister) == 0) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
            int value = ram.get(bank, instruction.getArguments()[1]); // Fetch value from given file register

            LOGGER.debug(String.format("ANDWF: Conjuncts content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value & workingRegister);

            } else {

                ram.set(bank, instruction.getArguments()[1], value & workingRegister);
            }

            //Checking for zero result
            if ((value & workingRegister) == 0) {

                setZeroFlag();

            } else {

                clearZeroFlag();
            }
        }
    }

    /**
     * The content of the workingRegister is OR'ed with the literal (here instructionArguments).
     * The Instruction Arguments are an Array with one Element.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    private void executeIORLW(Instruction instruction) {

        LOGGER.debug(String.format("IORLW: Inclusive disjunction of literal 0x%02X with working register", instruction.getArguments()[0]));

        setWorkingRegister(instruction.getArguments()[0] | workingRegister);

        //checking for a zero result after the OR operation.

        if (0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * The contents of the W register are XOR’ed with the eight bit literal 'k'.
     * The result is placed in the W register.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    private void executeXORLW(Instruction instruction) {

        LOGGER.debug(String.format("XORLW: Exclusive disjunction of literal 0x%02X with working register", instruction.getArguments()[0]));

        setWorkingRegister(instruction.getArguments()[0] ^ workingRegister);

        //checking for a zero result after the OR operation.

        if (0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * Exclusive OR the contents of the W register with register 'f'.
     * If 'd' is 0 the result is stored in the W register.
     * If 'd' is 1 the result is stored back in register 'f'.
     *
     * @param instruction Instruction consisting out of OPC and arguments.
     */

    private void executeXORWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = ram.get(bank, address);

            LOGGER.debug(String.format("XORWF: Exclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value ^ workingRegister);

            } else {

                ram.set(bank, address, value ^ workingRegister);

            }

            //Checking for zero result
            if ((value ^ workingRegister) == 0) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }
        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("XORWF: Exclusive disjunction of content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value ^ workingRegister);

            } else {

                ram.set(bank, instruction.getArguments()[1], value ^ workingRegister);

            }

            //Checking for zero result
            if ((value ^ workingRegister) == 0) {

                setZeroFlag();
            } else {

                clearZeroFlag();
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

    private void executeSUBWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = ram.get(bank, address);

            LOGGER.debug(String.format("SUBLW: Subtracts content at address 0x%02X in %s from working register", address, bank));

            // Check if digit carry is occurring

            if ((value & 0x000F) + ((~workingRegister + 1) & 0x000F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow
            if (255 > (instruction.getArguments()[0] & 0x00FF) + ((~workingRegister + 1) & 0x00FF)) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result.
            if (0 == workingRegister - value) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(workingRegister - value);

            } else {

                ram.set(bank, address, workingRegister - value);
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetch value from given file register
            int value = ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("SUBLW: Subtracts content at address 0x%02X in %s from working register", instruction.getArguments()[1], bank));

            if ((value & 0x000F) + ((~workingRegister + 1) & 0x000F) > 0xF) {

                setDigitCarryFlag();

            } else {

                clearDigitCarryFlag();
            }

            // Check for an arithmetic number overflow
            if (255 > (instruction.getArguments()[0] & 0x00FF) + ((~workingRegister + 1) & 0x00FF)) {

                setCarryFlag();

            } else {

                clearCarryFlag();
            }

            // Check for zero result.
            if (0 == workingRegister - value) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(workingRegister - value);

            } else {

                ram.set(bank, instruction.getArguments()[1], workingRegister - value);
            }
        }
    }

    /**
     * Push the current address as return point to stack and calls a
     * subroutine. This basically means that it leaves the current control
     * flow by jumping to another address.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeCALL(Instruction instruction) {

        /*
        Save address of next instruction to stack memory
         */

        stack.push(programCounter);

        /*
        Consists out of the opcode/address given as argument and the upper bits
        (bit 3 + 4) of PCLATH register.
         */

        int pclathBits = (ram.get(RamMemory.SFR.PCLATH) & 0b0001_1000) << 8;

        programCounter = instruction.getArguments()[0]; // Load jump address
        programCounter &= 0b00111_1111_1111; // Clear upper two bits
        programCounter = programCounter | pclathBits; // Adding PCLATH

        LOGGER.debug(String.format("CALL: Stores return address 0x%04X and calls subroutine at 0x%04X", stack.top(), programCounter));
    }

    /**
     * Returns from subroutine. Address of next instruction is poped from stack
     * memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeRETURN(Instruction instruction) {

        /*
        Restores address of next instruction from stack memory
         */

        programCounter = stack.pop();

        LOGGER.debug(String.format("RETURN: Return from subroutine to 0x%04X", programCounter));
    }

    /**
     * Returns a value from subroutine. Address of next instruction is poped from stack
     * memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeRETLW(Instruction instruction) {

        /*
        Restores address of next instruction from stack memory
         */

        programCounter = stack.pop();
        setWorkingRegister(instruction.getArguments()[0]); // Stores return value

        LOGGER.debug(String.format("RETLW: Return from subroutine to 0x%04X with value 0x%02X", programCounter, instruction.getArguments()[0]));

        // Check for zero value
        if (0 == workingRegister) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * Makes a jump to the given address inside of program memory.
     *
     * @param instruction Instruction consisting out of OPC and arguments
     */

    private void executeGOTO(Instruction instruction) {

        /*
        Consists out of the opcode/address given as argument and the upper bits
        (bit 3 + 4) of PCLATH register.
         */

        int pclathBits = (ram.get(RamMemory.SFR.PCLATH) & 0b0001_1000) << 8;

        programCounter = instruction.getArguments()[0]; // Load jump address
        programCounter &= 0b00111_1111_1111; // Clear upper two bits
        programCounter = programCounter | pclathBits; // Adding PCLATH

        LOGGER.debug(String.format("GOTO: Goes to instruction at 0x%04X", programCounter));
    }

    /**
     * Move data from W register to register 'f'
     * @param instruction Instruction consisting out of OPC and arguments
     */
    private void executeMOVWF(Instruction instruction) {

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVWF: Moves data from working register to address 0x%02X in %s", address, bank));

            // Moving data from W register to 'f' register
            ram.set(bank, address, workingRegister);


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVWF: Moves data from working register to address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Moving data from W register to 'f' register
            ram.set(bank, instruction.getArguments()[1], workingRegister);
        }
    }

    /**
     * The contents of register ’f’ are cleared
     * and the Z bit is set.
     * @param instruction Instruction consisting out of OPC and arguments
     */
    private void executeCLRF(Instruction instruction){

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("CLRF: Clears data from register at address 0x%02X in %s", address, bank));

            // Moving data from W register to 'f' register
            ram.set(bank, address, 0);

            // Setting Zero Flag
            setZeroFlag();

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("CLRF: Clears data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Moving data from W register to 'f' register
            ram.set(bank, instruction.getArguments()[1], 0);

            // Setting Zero Flag
            setZeroFlag();
        }
    }

	 /**
	  * The contents of register ’f’ are complemented. If ’d’ is 0 the result is stored in
	  * W. If ’d’ is 1 the result is stored back in
	  * register ’f’.
	  * @param instruction Instruction consisting out of OPC and arguments
	  */
	 private void executeCOMF(Instruction instruction){

		  if (0 == instruction.getArguments()[1]) { //Indirect addressing.

				// Get the lower 7 Bits of FSR if indirect addressing
				int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

				// Determine selected bank
				RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

				// Fetching value
				int value = ram.get(bank, address);


				// Checking for Zero result
				if (0 == ~value) {

					 setZeroFlag();
				} else {

					 clearZeroFlag();
				}

				LOGGER.debug(String.format("COMF: Complementing data from register at address 0x%02X in %s", address, bank));

				//Checking for destination.
				if (instruction.getArguments()[0] == 0) {

                    setWorkingRegister(~value);

				} else {

					 ram.set(bank, address, ~value);
				}


		  } else { //Direct addressing.

				// Determine selected bank
				RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

				// Fetching value
				int value = ram.get(bank, instruction.getArguments()[1]);

				// Checking for Zero result
				if (0 == ~value) {

					 setZeroFlag();
				} else {

					 clearZeroFlag();
				}

				LOGGER.debug(String.format("COMF: Complementing data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

				//Checking for destination.
				if (instruction.getArguments()[0] == 0) {

                    setWorkingRegister(~value);

				} else {

					 ram.set(bank, instruction.getArguments()[1], ~value);
				}
		  }
	 }

    /**
     * Decrement register ’f’. If ’d’ is 0 the
     * result is stored in the W register. If ’d’ is
     * 1 the result is stored back in register ’f’.
     * @param instruction Instruction consisting out of OPC and arguments
     */
	 private void executeDECF(Instruction instruction){

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, address);


            LOGGER.debug(String.format("DECF: Decrements data from register at address 0x%02X in %s", address, bank));

            // Checking for Zero result
            if (0 == value-1) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (0 > value-1 && instruction.getArguments()[0] == 0){

                setWorkingRegister(0xFF);

            }else if (0 > value-1){

                ram.set(bank, address, 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value - 1);

            } else {

                ram.set(bank, address, value-1);
            }



        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("DECF: Decrements data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Checking for Zero result
            if (0 == value-1) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (0 > value-1 && instruction.getArguments()[0] == 0){

                setWorkingRegister(0xFF);

            }else if (0 > value-1){

                ram.set(bank, instruction.getArguments()[1], 0xFF);
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value - 1);

            } else {

                ram.set(bank, instruction.getArguments()[1], value-1);
            }
        }
    }

    /**
     * The contents of register ’f’ are incremented. If ’d’ is 0 the result is placed in
     * the W register. If ’d’ is 1 the result is
     * placed back in register ’f’.
     * @param instruction Instruction consisting out of OPC and arguments
     */
    private void executeINCF(Instruction instruction){

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, address);


            LOGGER.debug(String.format("INCF: Increments data from register at address 0x%02X in %s", address, bank));

            // Checking for Zero result
            if (0 == value+1) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (255 < value+1 && instruction.getArguments()[0] == 0){

                setWorkingRegister(0x00);
                setZeroFlag();

            }else if (255 < value+1){

                ram.set(bank, address, 0x00);
                setZeroFlag();
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value + 1);

            } else {

                ram.set(bank, address, value+1);
            }



        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, instruction.getArguments()[1]);

            LOGGER.debug(String.format("INCF: Increments data from register at address 0x%02X in %s", instruction.getArguments()[1], bank));

            // Checking for Zero result
            if (0 == value+1) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }
            // Checking if value gets negative after decrementing.
            if (255 < value+1 && instruction.getArguments()[0] == 0){

                setWorkingRegister(0x00);
                setZeroFlag();

            }else if (255 < value+1){

                ram.set(bank, instruction.getArguments()[1], 0x00);
                setZeroFlag();
            }
            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value + 1);

            } else {

                ram.set(bank, instruction.getArguments()[1], value+1);
            }
        }
    }

    /**
     * The contents of register f is moved to a
     * destination dependant upon the status
     * of d. If d = 0, destination is W register. If
     * d = 1, the destination is file register f
     * itself. d = 1 is useful to test a file register since status flag Z is affected.
     * @param instruction Instruction consisting out of OPC and arguments
     */
    private void executeMOVF(Instruction instruction){
        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVF: Moves data from register at address 0x%02X in %s to Working register or itself", address, bank));

            int value = ram.get(bank, address);

            // Checking for Zero Flag
            if (0 == value) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value);

            } else {

                ram.set(bank, address, value);
            }

        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;


            LOGGER.debug(String.format("MOVF: Moves data from register at address 0x%02X in %s to Working register or itself", instruction.getArguments()[1], bank));

            int value = ram.get(bank, instruction.getArguments()[1]);

            // Checking for Zero Flag
            if (0 == value) {

                setZeroFlag();
            } else {

                clearZeroFlag();
            }


            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(value);

            } else {

                ram.set(bank, instruction.getArguments()[1], value);
            }
        }
    }

    /**
     * Inclusive OR the W register with register ’f’. If ’d’ is 0 the result is placed in the
     * W register. If ’d’ is 1 the result is placed
     * back in register ’f’.
     * @param instruction Instruction consisting out of OPC and arguments
     */
    private void executeIORWF(Instruction instruction){

        if (0 == instruction.getArguments()[1]) { //Indirect addressing.

            // Get the lower 7 Bits of FSR if indirect addressing
            int address = ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            // Determine selected bank
            RamMemory.Bank bank = 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, address);

            byte firstByte = (byte)(value & 0xFF);

            byte firstBits = (byte)((firstByte & 0x0F) << 4);
            byte lastBits = (byte)((firstByte & 0xF0) >> 4);

            int returnValue = (firstBits + lastBits);
            LOGGER.debug(String.format("IORWF: Inclusive disjunction of content at address 0x%02X in %s with working register", address, bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(returnValue);

            } else {

                ram.set(bank, address, returnValue);
            }


        } else { //Direct addressing.

            // Determine selected bank
            RamMemory.Bank bank = 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

            // Fetching value
            int value = ram.get(bank, instruction.getArguments()[1]);

            byte firstByte = (byte)(value & 0xFF);

            byte firstBits = (byte)((firstByte & 0x0F) << 4);
            byte lastBits = (byte)((firstByte & 0xF0) >> 4);

            int returnValue = (firstBits + lastBits);

            LOGGER.debug(String.format("IORWF: Inclusive disjunction of content at address 0x%02X in %s with working register", instruction.getArguments()[1], bank));

            //Checking for destination.
            if (instruction.getArguments()[0] == 0) {

                setWorkingRegister(returnValue);

            } else {

                ram.set(bank, instruction.getArguments()[1], returnValue);
            }
        }
    }
}
