package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.*;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.ai2ra.hso.simpic16f84.sim.vm.InstructionDecoder;
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

public final class InstructionExecutor implements ObservableExecution {

    private static final Logger LOGGER;

    /**
     * Working register used as accumulator.
     */
    private Integer workingRegister;
    /** Contains the next instruction before it's execution. */
    private Integer instructionRegister;
    /** The instruction pointer that points to the next instruction in program memory. */
    private Integer programCounter;

    /*
    Intentionally package-private to allow execution units direct access.
     */

    ProgramMemory<Integer> programMemory;
    RamMemory<Integer> ram;
    StackMemory<Integer> stack;
    EepromMemory<Integer> eeprom;

    /**
     * Part of ALU that is responsible for literal operations.
     */
    private LiteralExecutionUnit literalExecutionUnit;
    /**
     * Part of ALU that is responsible for jump operations.
     */
    private JumpExecutionUnit jumpExecutionUnit;
    /**
     * Part of ALU that is responsible for byte/control operations.
     */
    private ByteAndControlExecutionUnit byteAndControlExecutionUnit;
    /**
     * Part of ALU that is responsible for bit operations.
     */
    private BitExecutionUnit bitExecutionUnit;

    /** Used for synchronizing the execution flow. */
    private ReentrantLock lock;
    /** Used for supporting property changes of the internal state. */
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
        this.programMemory = programMemory;
        this.ram = ram;
        this.stack = stack;
        this.eeprom = eeprom;

        this.literalExecutionUnit = new LiteralExecutionUnit(this);
        this.jumpExecutionUnit = new JumpExecutionUnit(this);
        this.byteAndControlExecutionUnit = new ByteAndControlExecutionUnit(this);
        this.bitExecutionUnit = new BitExecutionUnit(this);

        lock = new ReentrantLock();
        changes = new PropertyChangeSupport(this);

        setInstructionRegister(0);
        setProgramCounter(0);
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

            /*
            Setters are used to notify observers automatically.
             */

            setInstructionRegister(programMemory.get(programCounter));
            setProgramCounter(programCounter + 1);

            // Decode current instruction

            Instruction instruction = InstructionDecoder.decode(null == instructionRegister ? 0 : instructionRegister);

            switch (instruction.getOpc()) {

                // Literal operations

                case ADDLW: {

                    literalExecutionUnit.executeADDLW(instruction);
                    break;
                }
                case ANDLW: {

                    literalExecutionUnit.executeANDLW(instruction);
                    break;
                }
                case MOVLW: {

                    literalExecutionUnit.executeMOVLW(instruction);
                    break;
                }
                case SUBLW: {

                    literalExecutionUnit.executeSUBLW(instruction);
                    break;
                }
                case IORLW: {

                    literalExecutionUnit.executeIORLW(instruction);
                    break;
                }
                case XORLW: {

                    literalExecutionUnit.executeXORLW(instruction);
                    break;
                }
                case RETLW: {

                    literalExecutionUnit.executeRETLW(instruction);
                    break;
                }

                // Jump operations

                case CALL: {

                    jumpExecutionUnit.executeCALL(instruction);
                    break;
                }
                case GOTO: {

                    jumpExecutionUnit.executeGOTO(instruction);
                    break;
                }

                // Byte/Control operations

                case ADDWF: {

                    byteAndControlExecutionUnit.executeADDWF(instruction);
                    break;
                }
                case CLRW: {

                    byteAndControlExecutionUnit.executeCLRW();
                    break;
                }
                case ANDWF: {

                    byteAndControlExecutionUnit.executeANDWF(instruction);
                    break;
                }
                case XORWF: {

                    byteAndControlExecutionUnit.executeXORWF(instruction);
                    break;
                }
                case SUBWF: {

                    byteAndControlExecutionUnit.executeSUBWF(instruction);
                    break;
                }
                case RETURN: {

                    byteAndControlExecutionUnit.executeRETURN(instruction);
                    break;
                }
                case MOVWF: {

                    byteAndControlExecutionUnit.executeMOVWF(instruction);
                    break;
                }
                case CLRF: {

                    byteAndControlExecutionUnit.executeCLRF(instruction);
                    break;
                }
                case COMF: {

                    byteAndControlExecutionUnit.executeCOMF(instruction);
                    break;
                }
                case DECF: {

                    byteAndControlExecutionUnit.executeDECF(instruction);
                    break;
                }
                case DECFSZ: {

                    byteAndControlExecutionUnit.executeDECFSZ(instruction);
                }
                case INCF: {

                    byteAndControlExecutionUnit.executeINCF(instruction);
                    break;
                }
                case INCFSZ: {

                    byteAndControlExecutionUnit.executeINCFSZ(instruction);
                }
                case MOVF: {

                    byteAndControlExecutionUnit.executeMOVF(instruction);
                    break;
                }
                case IORWF: {

                    byteAndControlExecutionUnit.executeIORWF(instruction);
                    break;
                }
                case RRF: {

                    byteAndControlExecutionUnit.executeRRF(instruction);
                }
                case RLF: {

                    byteAndControlExecutionUnit.executeRLF(instruction);
                }
                case NOP:

                 // Bit-oriented instructions:

                case BCF: {

                    bitExecutionUnit.executeBCF(instruction);
                }
                case BSF: {

                    bitExecutionUnit.executeBSF(instruction);
                    break;
                }
                case BTFSC: {

                    bitExecutionUnit.executeBTFSC(instruction);
                    break;
                }
                case BTFSS: {

                    bitExecutionUnit.executeBTFSS(instruction);
                    break;
                }
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

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        changes.addPropertyChangeListener(listener);
    }

    /**
     * Removes a change listener <b>only</b> from the change support of the executor.
     *
     * @param listener The listener that should be removed
     */

    @Override
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

    void setWorkingRegister(Integer value) {

        changes.firePropertyChange("workingRegister", workingRegister, value);
        workingRegister = value;
    }

    /**
     * Used for fetching the content of the working register.
     *
     * @return Returns the current content of the working register
     */

    @Override
    public Integer getWorkingRegister() {

        return workingRegister;
    }

    /**
     * Used for changing content of program counter. Moreover this method allows
     * notifying all observers.
     *
     * @param value The value that should be written to program counter
     */

    void setProgramCounter(Integer value) {

        changes.firePropertyChange("programCounter", programCounter, value);
        programCounter = value;
    }

    /**
     * Used for fetching the content of the program counter.
     *
     * @return Returns the current content of the program counter
     */

    @Override
    public Integer getProgramCounter() {

        return programCounter;
    }

    /**
     * Used for changing content of instruction register. Moreover this method allows
     * notifying all observers.
     *
     * @param value The value that should be written to instruction register
     */

    private void setInstructionRegister(Integer value) {

        changes.firePropertyChange("instructionRegister", instructionRegister, value);
        instructionRegister = value;
    }

    /**
     * Used for fetching the content of the instruction register.
     *
     * @return Returns the current content of the instruction register
     */

    @Override
    public Integer getInstructionRegister() {

        return instructionRegister;
    }

    /**
     * Sets the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    void setDigitCarryFlag() {

        LOGGER.info("Set 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000010));
    }

    /**
     * Clears the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    void clearDigitCarryFlag() {

        LOGGER.info("Clear 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111101));
    }

    /**
     * Sets the carry flag inside of status register {@link RamMemory RAM}.
     */

    void setCarryFlag() {

        LOGGER.info("Set 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000001));
    }

    /**
     * Clears the carry flag inside of status register {@link RamMemory RAM}.
     */

    void clearCarryFlag() {

        LOGGER.info("Clear 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111110));
    }

    /**
     * Sets the zero flag inside of status register {@link RamMemory RAM}.
     */

    void setZeroFlag() {

        LOGGER.info("Set 'Zero' (Z) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) | 0b00000100));
    }

    /**
     * Clears the zero flag inside of status register {@link RamMemory RAM}.
     */

    void clearZeroFlag() {

        LOGGER.info("Clear 'Zero' (Z) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (ram.get(RamMemory.SFR.STATUS) & 0b11111011));
    }

    /**
     * Returns the RP0 bit of the STATUS register. This bit is used for <b>direct</b>
     * addressing, if bit is set (RP0 = 1) second bank is selected, otherwise the first.
     *
     * @return Returns 0 if first bank is selected, otherwise a none 0 value
     */

    int getRP0Bit() {

        return (ram.get(RamMemory.SFR.STATUS) & 0b0010_0000) >> 5;
    }

    /**
     * Returns the IRP bit of the STATUS register. This bit is used for
     * <b>indirect</b> addressing.
     *
     * @return Returns 0 if first bank is selected, otherwise a none 0 value
     */

    int getIRPBit() {

        return (ram.get(RamMemory.SFR.STATUS) & 0b1000_0000) >> 7;
    }
}
