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
 * <p>
 *     Since latest improvements the concrete instruction implementations are outsourced
 *     to separate ALUs/executor units. This class just redirect the execution task and
 *     provide useful utilities for it's sub modules (executors).
 * </p>
 *
 * Some internally used properties like counters are originally stored/implemented inside this
 * class:
 *
 * <ul>
 *     <li>Working Register (Accumulator)</li>
 *     <li>Instruction Register (IR)</li>
 *     <li>Program Counter (PC)</li>
 *     <li>Runtime Counter (RC)</li>
 *     <li>Frequency (Quartz Frequency)</li>
 * </ul>
 *
 * For general usage this are the important methods from the outside:
 *
 * <pre>{@code
 * InstructionExecutor executor = new InstructionExecutor(...);
 * executor.execute(); // Executes next instruction
 * executor.reset(); // Resets to power-on state
 * }</pre>
 *
 * @author Freddy1096
 * @author 0x1C1B
 * @see InstructionDecoder
 */

public class InstructionExecutor implements ObservableExecution {

    private static final Logger LOGGER;

    // Additional registers and counters

    /**
     * Working register used as accumulator.
     */
    private Byte workingRegister;
    /** Contains the next instruction before it's execution. */
    private Short instructionRegister;
    /** The instruction pointer that points to the next instruction in program memory. */
    private Integer programCounter;
    /** Runtime counter indicates execution time. */
    private Double runtimeCounter;
    /** Current quartz frequency, indirectly the execution speed. */
    private Double frequency;

    // Memory RAM + FLASH + EEPROM (intentionally package-private)

    private ProgramMemory<Short> programMemory;
    private EepromMemory<Byte> eeprom;
    RamMemory<Byte> ram;
    StackMemory<Integer> stack;

    // Arithmetic and Logic Unit (ALU)

    /** Part of ALU that is responsible for literal operations. */
    private LiteralExecutionUnit literalExecutionUnit;
    /** Part of ALU that is responsible for jump operations. */
    private JumpExecutionUnit jumpExecutionUnit;
    /** Part of ALU that is responsible for byte/control operations. */
    private ByteAndControlExecutionUnit byteAndControlExecutionUnit;
    /** Part of ALU that is responsible for bit operations. */
    private BitExecutionUnit bitExecutionUnit;

    /** Used for synchronizing the execution flow. */
    private ReentrantLock lock;
    /** Used for supporting property changes of the internal state. */
    private PropertyChangeSupport changes;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    /**
     * Constructs a new execution unit by injecting required memory dependencies. Following this,
     * all related resources are reset to the power-on state.
     *
     * @param programMemory The program memory which contains the executable program
     * @param ram The RAM consisting out of SFR's and GPR's
     * @param stack The stack memory, should contain at least eight levels
     * @param eeprom The EEPROM for persisting data beyond restarts
     */

    public InstructionExecutor(ProgramMemory<Short> programMemory, RamMemory<Byte> ram,
                               StackMemory<Integer> stack, EepromMemory<Byte> eeprom) {

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

        // Initialize counters already

        this.frequency = 4_000_000.0; // 4MHz
        this.runtimeCounter = 0.0;
    }

    /**
     * Loads, decodes and executes the next instruction inside of program memory. Important
     * to note is that just <b>one</b> instruction is executed per method call. A typical
     * execution cycle includes the following:
     *
     * <ol>
     *     <li>
     *         Check for possible occurred interrupts. If occurred the ISR is called first.
     *     </li>
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
     * @throws IllegalStateException Thrown if requested operation is not supported
     */

    public int execute() throws IllegalStateException {

        lock.lock();

        try {

            handleInterrupts(); // Check and handle occurred interrupts

            LOGGER.info(String.format("Load OPC from 0x%04X into instruction register (IR)", programCounter));

            // Setters are used to notify observers automatically.

            setInstructionRegister(programMemory.get(programCounter));
            setProgramCounter(programCounter + 1);

            // Decode current instruction

            Instruction instruction = InstructionDecoder.decode(null == instructionRegister ? 0 : instructionRegister);

            switch (instruction.getOpc()) {

                // Literal operations

                case ADDLW: {

                    literalExecutionUnit.executeADDLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case ANDLW: {

                    literalExecutionUnit.executeANDLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case MOVLW: {

                    literalExecutionUnit.executeMOVLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case SUBLW: {

                    literalExecutionUnit.executeSUBLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case IORLW: {

                    literalExecutionUnit.executeIORLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case XORLW: {

                    literalExecutionUnit.executeXORLW(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case RETLW: {

                    literalExecutionUnit.executeRETLW(instruction);
                    updateRuntimeCounter(2);
                    break;
                }

                // Jump operations

                case CALL: {

                    jumpExecutionUnit.executeCALL(instruction);
                    updateRuntimeCounter(2);
                    break;
                }
                case GOTO: {

                    jumpExecutionUnit.executeGOTO(instruction);
                    updateRuntimeCounter(2);
                    break;
                }

                // Byte/Control operations

                case ADDWF: {

                    byteAndControlExecutionUnit.executeADDWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case CLRW: {

                    byteAndControlExecutionUnit.executeCLRW();
                    updateRuntimeCounter(1);
                    break;
                }
                case ANDWF: {

                    byteAndControlExecutionUnit.executeANDWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case XORWF: {

                    byteAndControlExecutionUnit.executeXORWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case SUBWF: {

                    byteAndControlExecutionUnit.executeSUBWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case RETURN: {

                    byteAndControlExecutionUnit.executeRETURN();
                    updateRuntimeCounter(2);
                    break;
                }
                case MOVWF: {

                    byteAndControlExecutionUnit.executeMOVWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case CLRF: {

                    byteAndControlExecutionUnit.executeCLRF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case COMF: {

                    byteAndControlExecutionUnit.executeCOMF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case DECF: {

                    byteAndControlExecutionUnit.executeDECF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case DECFSZ: {

                    byteAndControlExecutionUnit.executeDECFSZ(instruction);
                    updateRuntimeCounter(2);
                    break;
                }
                case INCF: {

                    byteAndControlExecutionUnit.executeINCF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case INCFSZ: {

                    byteAndControlExecutionUnit.executeINCFSZ(instruction);
                    updateRuntimeCounter(2);
                    break;
                }
                case MOVF: {

                    byteAndControlExecutionUnit.executeMOVF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case IORWF: {

                    byteAndControlExecutionUnit.executeIORWF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case RRF: {

                    byteAndControlExecutionUnit.executeRRF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case RLF: {

                    byteAndControlExecutionUnit.executeRLF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case NOP: {

                    byteAndControlExecutionUnit.executeNOP();
                    updateRuntimeCounter(1);
                    break;
                }
                case SWAPF: {

                    byteAndControlExecutionUnit.executeSWAPF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case RETFIE: {

                    byteAndControlExecutionUnit.executeRETFIE(instruction);
                    updateRuntimeCounter(2);
                    break;
                }

                // Bit operations

                case BCF: {

                    bitExecutionUnit.executeBCF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case BSF: {

                    bitExecutionUnit.executeBSF(instruction);
                    updateRuntimeCounter(1);
                    break;
                }
                case BTFSC: {

                    bitExecutionUnit.executeBTFSC(instruction);
                    updateRuntimeCounter(2);
                    break;
                }
                case BTFSS: {

                    bitExecutionUnit.executeBTFSS(instruction);
                    updateRuntimeCounter(2);
                    break;
                }
                default: {

                    throw new IllegalStateException("Unsupported instruction code");
                }
            }

        } catch (MemoryIndexOutOfBoundsException exc) {

            LOGGER.error("Unimplemented address accessed", exc);

        } catch (UnsupportedOperationException exc) {

            LOGGER.error("Unsupported operation code found", exc);

        } finally {

            lock.unlock();
        }

        updateTimer();
        return programCounter;
    }

    /**
     * Resets status of RAM and working register to the power-on state. The power-on
     * state is defined inside of the data sheet. Moreover additional registers and
     * counters are reset (W, IR, PC, RC).
     */

    public void reset() {

        LOGGER.info("Reset registers and memory to power-on state");

        setWorkingRegister((byte) 0x00);
        setProgramCounter(0x00);
        setInstructionRegister((short) 0x00);

        // Initialize runtime counter

        setRuntimeCounter(0x00);
        setFrequency(4_000_000.0 /* 4MHz */);

        // Initialize the special function registers

        ram.set(RamMemory.SFR.INDF, (byte) 0x00);
        ram.set(RamMemory.SFR.TMR0, (byte) 0x00);
        ram.set(RamMemory.SFR.PCL, (byte) 0x00);
        ram.set(RamMemory.SFR.STATUS, (byte) 0b0001_1100);
        ram.set(RamMemory.SFR.FSR, (byte) 0x000);
        ram.set(RamMemory.SFR.PORTA, (byte) 0x00);
        ram.set(RamMemory.SFR.PORTB, (byte) 0x00);
        ram.set(RamMemory.SFR.EEDATA, (byte) 0x00);
        ram.set(RamMemory.SFR.EEADR, (byte) 0x00);
        ram.set(RamMemory.SFR.PCLATH, (byte) 0x00);
        ram.set(RamMemory.SFR.INTCON, (byte) 0x00);
        ram.set(RamMemory.SFR.OPTION, (byte) 0b1111_1111);
        ram.set(RamMemory.SFR.TRISA, (byte) 0b0001_1111);
        ram.set(RamMemory.SFR.TRISB, (byte) 0b1111_1111);
        ram.set(RamMemory.SFR.EECON1, (byte) 0x00);
        ram.set(RamMemory.SFR.EECON2, (byte) 0x00);
    }

    /**
     * Adds a change listener <b>only</b> for observing the executor's state. This pattern
     * is specially intended to use for the additional registers and counters.
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

    /**
     * Used for changing content of working register. Moreover this method allows
     * notifying all observers.
     *
     * @param value The value that should be written to working register
     */

    void setWorkingRegister(Byte value) {

        changes.firePropertyChange("workingRegister", workingRegister, value);
        workingRegister = value;
    }

    /**
     * Used for fetching the content of the working register.
     *
     * @return Returns the current content of the working register
     */

    @Override
    public Byte getWorkingRegister() {

        return workingRegister;
    }

    /**
     * Sets runtime counter to a given count.
     *
     * @param counter The given count
     */

    private void setRuntimeCounter(double counter) {

        changes.firePropertyChange("runtimeCounter", runtimeCounter, counter);
        runtimeCounter = counter;
    }

    /**
     * Increases the runtime counter dependent to the quartz frequency.
     *
     * @param cycles The number of cycles that were used for executing the last instruction
     */

    private void updateRuntimeCounter(int cycles) {

        double timePerCycle = 4000000.0 / frequency;
        double oldRuntimeCounter = runtimeCounter;

        runtimeCounter = runtimeCounter + (timePerCycle * cycles);
        changes.firePropertyChange("runtimeCounter", oldRuntimeCounter, runtimeCounter);
    }

    /**
     * Allows access to the runtime counter in micro seconds.
     *
     * @return Returns the current state of the runtime counter
     */

    @Override
    public Double getRuntimeCounter() {

        return runtimeCounter;
    }

    /**
     * Determines the current quartz frequency, implicitly the current execution speed.
     *
     * @return Returns the current quartz frequency in Hz
     */

    @Override
    public Double getFrequency() {

        return frequency;
    }

    /**
     * Allows changing the quartz frequency. Valid values are all values between 32kHz
     * and 20MHz.
     *
     * @param frequency The new execution frequency in Hz
     * @throws IllegalArgumentException Thrown if invalid frequency is provided
     */

    @Override
    public void setFrequency(Double frequency) throws IllegalArgumentException {

        if (32_000 > frequency || 20_000_000 < frequency) {

            throw new IllegalArgumentException("Frequency is only valid between 32kHz and 20MHz");
        }

        this.frequency = frequency;
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

    private void setInstructionRegister(Short value) {

        changes.firePropertyChange("instructionRegister", instructionRegister, value);
        instructionRegister = value;
    }

    /**
     * Used for fetching the content of the instruction register.
     *
     * @return Returns the current content of the instruction register
     */

    @Override
    public Short getInstructionRegister() {

        return instructionRegister;
    }

    /**
     * Sets the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    @SuppressWarnings("WeakerAccess")
    void setDigitCarryFlag() {

        LOGGER.info("Set 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) | 0b00000010));
    }

    /**
     * Clears the digit carry flag inside of status register {@link RamMemory RAM}.
     */

    @SuppressWarnings("WeakerAccess")
    void clearDigitCarryFlag() {

        LOGGER.info("Clear 'Digit Carry' (DC) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) & 0b11111101));
    }

    /**
     * Sets the carry flag inside of status register {@link RamMemory RAM}.
     */

    void setCarryFlag() {

        LOGGER.info("Set 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) | 0b00000001));
    }

    /**
     * Clears the carry flag inside of status register {@link RamMemory RAM}.
     */

    void clearCarryFlag() {

        LOGGER.info("Clear 'Carry' (C) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) & 0b11111110));
    }

    /**
     * Determines if the carry flag is set.
     *
     * @return Returns true if carry flag is set, otherwise false
     */

    boolean isCarryFlag() {

        return (ram.get(RamMemory.SFR.STATUS) & 0b0000_0001) != 0;
    }

    /**
     * Sets the zero flag inside of status register {@link RamMemory RAM}.
     */

    void setZeroFlag() {

        LOGGER.info("Set 'Zero' (Z) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) | 0b00000100));
    }

    /**
     * Clears the zero flag inside of status register {@link RamMemory RAM}.
     */

    @SuppressWarnings("WeakerAccess")
    void clearZeroFlag() {

        LOGGER.info("Clear 'Zero' (Z) flag inside of STATUS register");
        ram.set(RamMemory.SFR.STATUS, (byte) (ram.get(RamMemory.SFR.STATUS) & 0b11111011));
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

    /**
     * Utility method used for checking an overflow/underflow. If result is bigger than
     * 0xFF an overflow is occurred and the carry flag is set. <b>Please note:</b> This
     * kind of validation does only work for unsigned integers, this mean the result
     * parameter has to be an unsigned integer.
     *
     * @param result Result of the calculated expression as unsigned integer
     */

    void checkCarryFlag(int result) {

        if (0xFF < result) {

            setCarryFlag();

        } else {

            clearCarryFlag();
        }
    }

    /**
     * Helper method for checking if the result is zero. Flag is set if the expression's
     * result is equal to zero.
     *
     * @param result Result of the calculated expression
     */

    void checkZeroFlag(int result) {

        if (0 == result) {

            setZeroFlag();

        } else {

            clearZeroFlag();
        }
    }

    /**
     * Used for checking the digit carry flag. An expression related statement is evaluated, if
     * it's true the digit carry flag is set, otherwise not.
     *
     * @param result Result of the calculated expression
     */

    void checkDigitCarryFlag(boolean result) {

        if (result) {

            setDigitCarryFlag();

        } else {

            clearDigitCarryFlag();
        }
    }

    /**
     * Internal helper method that determines if indirect addressing is used or not.
     *
     * @param instruction The actual instruction
     * @return Returns true if indirect addressing is used, otherwise false
     */

    private boolean usesIndirectAddressing(Instruction instruction) {

        /*
        Two kind of instructions with file address exists, one with additional destination
        bit and one without.
         */

        if (1 == instruction.getArguments().length) { // Without additional destination bit

            return 0 == instruction.getArguments()[0];

        } else if (2 == instruction.getArguments().length) { // Destination bit exists

            return 0 == instruction.getArguments()[1];
        }

        throw new IllegalArgumentException("Instruction doesn't have a file register address as argument");
    }

    /**
     * Determines the addressed file register. It supports direct and indirect addressing with
     * or without additional destination bit.
     *
     * @param instruction The instruction for extracting the file register address
     * @return Returns the determined file register address
     * @throws IllegalArgumentException Thrown if given instruction doesn't have a file address as argument
     */

    int getFileAddress(Instruction instruction) throws IllegalArgumentException {

        /*
        Two kind of instructions with file address exists, one with additional destination
        bit and one without.
         */

        if (1 == instruction.getArguments().length) { // Without additional destination bit

            if (0 == instruction.getArguments()[0]) { // Indirect addressing

                return ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            } else { // Direct addressing

                return instruction.getArguments()[0];
            }

        } else if (2 == instruction.getArguments().length) { // Destination bit exists

            if (0 == instruction.getArguments()[1]) { // Indirect addressing

                return ram.get(RamMemory.SFR.FSR) & 0b0111_1111;

            } else { // Direct addressing

                return instruction.getArguments()[1];
            }
        }

        throw new IllegalArgumentException("Instruction doesn't have a file register address as argument");
    }

    /**
     * Determines current select bank. It support direct and indirect addressing. For
     * succeeding it's required, that a file register address is provided as
     * instruction argument.
     *
     * @param instruction The actual instruction
     * @return Return the currently selected bank
     */

    RamMemory.Bank getSelectedBank(Instruction instruction) {

        /*
        Indirect addressing uses the IRP bit while direct addressing uses the RP0 bit.
         */

        if (usesIndirectAddressing(instruction)) {

            return 0 == getIRPBit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;

        } else {

            return 0 == getRP0Bit() ? RamMemory.Bank.BANK_0 : RamMemory.Bank.BANK_1;
        }
    }

    /**
     * Checks if the 0 Bit of EECON1 is set.
     * Returns true if it is and false if it isn't.
     * @return if the RD Bit of EECON1 is set or not.
     */
    boolean checkForRDBit() {

        byte register = ram.get(RamMemory.SFR.EECON1);

        //Checking if the 0 Bit of EECON 1 is set or not.
		  return (register & 0b0000_0001) == 0b0000_0001;
    }

    /**
     * Checks if the 1 Bit of EECON1 is set.
     * Returns true if it is and false if it isn't.
     * @return if the WR Bit of EECON1 is set or not.
     */
    boolean checkForWRBit() {

        byte register = ram.get(RamMemory.SFR.EECON1);

        //Checking if the 1 Bit of EECON 1 is set or not.
        return (register & 0b0000_0010) == 0b0000_0010;
    }

	 /**
	  * Checks if the 2 Bit of EECON1 is set.
	  * Returns true if it is and false if it isn't.
	  * @return if the WREN Bit of EECON1 is set or not.
	  */
    boolean checkForWRENBit() {

		  byte register = ram.get(RamMemory.SFR.EECON1);

		  //Checking if the 1 Bit of EECON 2 is set or not.
		  return (register & 0b0000_0100) == 0b0000_0100;
	 }

    /**
     * Checks if the 3 Bit of EECON2 is set.
     * Returns true if it is and false if it isn't.
     * @return if the WRERR Bit of EECON1 is set or not.
     */
	 boolean checkForWRERRBit() {

        byte register = ram.get(RamMemory.SFR.EECON1);

        //Checking if the 1 Bit of EECON 3 is set or not.
        return (register & 0b0000_1000) == 0b0000_1000;
    }

	 /**
	  * Checks if the 4 Bit of EECON2 is set.
	  * Returns true if it is and false if it isn't.
	  * @return if the EEIF Bit of EECON1 is set or not.
	  */
    boolean checkForEEIFBit() {

		  byte register = ram.get(RamMemory.SFR.EECON1);

		  //Checking if the 1 Bit of EECON 4 is set or not.
		  return (register & 0b0001_0000) == 0b0001_0000;
	 }

    /**
     * Update timer by incrementing it. Moreover it checks for timer overflows, if
     * a overflow occurred, the interrupt flag inside of the INTCON register is set.
     */

    private void updateTimer() {

        // Check for timer overflow

        if (0xFF == (0xFF & ram.get(RamMemory.SFR.TMR0))) {

            // Indicate interrupt by setting T0IF bit inside of INTCON register

            ram.set(RamMemory.SFR.INTCON, (byte) (ram.get(RamMemory.SFR.INTCON) | 0b0000_0100));
        }

        ram.set(RamMemory.SFR.TMR0, (byte) (ram.get(RamMemory.SFR.TMR0) + 1));
    }

    /**
     * Check and handle occurred interrupts. The default handling behaviour is calling the
     * Interrupt Service Routine. <b>Please note:</b> Because there's no default ISR, it's
     * assumed that the loaded program (LST file) contains an ISR at address <i>0x0004</i>.
     */

    private void handleInterrupts() {

        if (checkTMR0Interrupt()) {

            callISR(0x0004); // Calls ISR at address 0x0004
        }
    }

    /**
     * Utility method for calling the Interrupt Service Routine (ISR). This is a helper method
     * for the interrupt handling cycle. In addition this methods set the
     * Global Interrupt Enable (GIE) already.
     *
     * @param address Address of the Interrupt Service Routine (ISR)
     */

    private void callISR(int address) {

        // Enable the Global Interrupt Enable (GIE) bit before calling the ISR

        ram.set(RamMemory.SFR.INTCON, (byte) (ram.get(RamMemory.SFR.INTCON) & 0b0111_1111));

        stack.push(getProgramCounter()); // Save address of next instruction to stack memory

        /*
        Consists out of the opcode/address given as argument and the upper bits
        (bit 3 + 4) of PCLATH register.
         */

        int pclathBits = (ram.get(RamMemory.SFR.PCLATH) & 0b0001_1000) << 8;

        address &= 0b00111_1111_1111; // Clear upper two bits
        address |= pclathBits; // Adding PCLATH

        setProgramCounter(address);
    }

    /**
     * Determines if timer (TMR0) interrupt occurred.
     *
     * @return Returns true if TMR0 interrupt occurred otherwise false
     */

    private boolean checkTMR0Interrupt() {

        return (ram.get(RamMemory.SFR.INTCON) & 0b1010_0100) == 0xA4;
    }
}
