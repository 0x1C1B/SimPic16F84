package org.ai2ra.hso.simpic16f84.sim;

import org.ai2ra.hso.simpic16f84.sim.mem.*;
import org.ai2ra.hso.simpic16f84.sim.vm.AIRALstParser;
import org.ai2ra.hso.simpic16f84.sim.vm.exec.InstructionExecutor;
import org.ai2ra.hso.simpic16f84.sim.vm.LstParser;
import org.ai2ra.hso.simpic16f84.sim.vm.exec.ObservableExecution;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

/**
 * Pic16F84VM is the runtime environment of the simulator itself. It encapsulates the
 * required memory blocks as well as the control flow components. The former one
 * includes:
 *
 * <ul>
 *     <li>{@link ProgramMemory}</li>
 *     <li>{@link RamMemory}</li>
 *     <li>{@link StackMemory}</li>
 *     <li>{@link EepromMemory}</li>
 * </ul>
 *
 * Moreover it controls the whole execution flow. Therefor it holds the
 * {@link InstructionExecutor} utility. Some important methods to consider are the
 * {@link Pic16F84VM#load(File)} method, that's responsible for parsing and
 * loading a new program to memory, and the {@link Pic16F84VM#execute()} method,
 * that's executing the next instruction. In addition, it supports
 * observing important virtual machine states, exclusively the
 * {@link Pic16F84VM#loaded} and {@link Pic16F84VM#running} state, using
 * a registered {@link PropertyChangeListener}.
 *
 * @author 0x1C1B
 * @see InstructionExecutor
 * @see LstParser
 */

public class Pic16F84VM {

    private static final Logger LOGGER;

    private ProgramMemory<Short> programMemory;
    private RamMemory<Byte> ram;
    private StackMemory<Integer> stack;
    private EepromMemory<Byte> eeprom;

    private LstParser<Short> parser;
    private InstructionExecutor executor;
    private PropertyChangeSupport changes;

    /**
     * Indicates if the virtual machine already loaded a valid program
     */
    private boolean loaded;
    /** Determines if virtual machine is already running */
    private boolean running;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    /**
     * Initializes and creates a new usable Pic16F84 runtime environment.
     * This includes a instantiation of separate memory blocks as well as an
     * execution unit.
     */

    public Pic16F84VM() {

        this.programMemory = new ProgramMemory<>(1000);
        this.ram = new RamMemory<>();
        this.stack = new StackMemory<>(8);
        this.eeprom = new EepromMemory<>(64);

        this.parser = new AIRALstParser();
        this.executor = new InstructionExecutor(programMemory, ram, stack, eeprom);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Allows pseudo read-only access to the program memory. Important to note is,
     * that's pseudo read-only, this means that's possible to bypass this restriction
     * using a type cast. The feature that's intended to use with with methods, is
     * observing memory changes. Therefor, the returned instance provide the
     * {@link ObservableMemory#addPropertyChangeListener(PropertyChangeListener)}
     * method for registering a listener waiting for memory changes. The observed
     * property is named in the format <code>memory[%d]</code>, while the <i>%d</i> is
     * replaced by the memory address.
     *
     * @return Returns the program memory in a pseudo read-only state
     * @see ObservableMemory
     */

    public ObservableMemory<Short> getProgramMemory() {

        return programMemory;
    }

    /**
     * Allows pseudo read-only access to the data memory (RAM). Important to note is,
     * that's pseudo read-only, this means that's possible to bypass this restriction
     * using a type cast. The feature that's intended to use with with methods, is
     * observing memory changes. Therefor, the returned instance provide the
     * {@link ObservableMemory#addPropertyChangeListener(PropertyChangeListener)}
     * method for registering a listener waiting for memory changes. The observed
     * property is named in the format <code>bank0[%d]</code>/<code>bank1[%d]</code>
     * depending on which bank changed, while the <i>%d</i> is replaced by the
     * memory address.
     *
     * @return Returns the data memory in a pseudo read-only state
     * @see ObservableMemory
     */

    public ObservableMemory<Byte> getRam() {

        return ram;
    }

    /**
     * Allows pseudo read-only access to the stack memory. Important to note is,
     * that's pseudo read-only, this means that's possible to bypass this restriction
     * using a type cast. The feature that's intended to use with with methods, is
     * observing memory changes. Therefor, the returned instance provide the
     * {@link ObservableMemory#addPropertyChangeListener(PropertyChangeListener)}
     * method for registering a listener waiting for memory changes. The observed
     * property is named in the format <code>memory[%d]</code>, while the <i>%d</i> is
     * replaced by the memory address.
     *
     * @return Returns the stack memory in a pseudo read-only state
     * @see ObservableMemory
     */

    public ObservableMemory<Integer> getStack() {

        return stack;
    }

    /**
     * Allows pseudo read-only access to the data memory (EEPROM). Important to note is,
     * that's pseudo read-only, this means that's possible to bypass this restriction
     * using a type cast. The feature that's intended to use with with methods, is
     * observing memory changes. Therefor, the returned instance provide the
     * {@link ObservableMemory#addPropertyChangeListener(PropertyChangeListener)}
     * method for registering a listener waiting for memory changes. The observed
     * property is named in the format <code>memory[%d]</code>, while the <i>%d</i> is
     * replaced by the memory address.
     *
     * @return Returns the data memory in a pseudo read-only state
     * @see ObservableMemory
     */

    public ObservableMemory<Byte> getEeprom() {

        return eeprom;
    }

    /**
     * Returns the main execution unit (CPU + ALU) in a readable state. This is primarily used
     * for observing internal changes.
     *
     * @return Returns the internally used instruction executor
     */

    public ObservableExecution getExecutor() {

        return executor;
    }

    /**
     * Adds a change listener <b>only</b> for observing the virtual machines state. For
     * observing memory changes, the listeners must be registered for the related memory
     * structures.
     *
     * @param listener The listener that should be registered
     */

    public void addPropertyChangeListener(PropertyChangeListener listener) {

        changes.addPropertyChangeListener(listener);
    }

    /**
     * Removes a change listener <b>only</b> from the change support of the virtual
     * machine, <b>not</b> from a memory block.
     *
     * @param listener The listener that should be removed
     */

    public void removePropertyChangeListener(PropertyChangeListener listener) {

        changes.removePropertyChangeListener(listener);
    }

    /**
     * Loads the machine instructions of an LST file to program memory. Calling this
     * method is required for executing any kind of program.
     *
     * @param file The path to the LST file
     * @throws IOException           Thrown if given file couldn't be loaded
     * @throws NumberFormatException Thrown if LST file is malformed, means couldn't be parsed
     */

    public void load(File file) throws IOException {

        stop(); // Stops current execution flow if runtime environment is already running

        Short[] instructions = parser.parse(file); // Extract machine instructions

        // Load extracted machine instructions into program memory

        for (int address = 0; address < instructions.length; ++address) {

            programMemory.set(instructions[address], address);
        }

        loaded = true; // Set state to execution ready
        changes.firePropertyChange("loaded", false, true);
        executor.reset();
    }

    /**
     * Executes the next instruction cycle, basically just the next instruction.
     * Important to note is, that if it's called the first time for a newly loaded
     * program, it resets the execution unit with all memory blocks. Moreover for
     * allowing observing the execution flow, every time this methods succeeds the
     * address of the next instruction is returned.
     *
     * @throws IllegalStateException Thrown if no valid program was previously loaded
     * @see Pic16F84VM#load(File)
     * @return Returns the address of next instruction
     */

    public int execute() {

        if (!loaded) {

            throw new IllegalStateException("No executable program loaded");

        } else {

            // Reset runtime state at first time

            if (!running) {

                running = true;
                changes.firePropertyChange("running", false, true);

                executor.reset();
            }

            return executor.execute();
        }
    }

    /**
     * Breaks the current execution flow. Execution must be restarted after calling
     * this method.
     */

    public void stop() {

        running = false;
        changes.firePropertyChange("running", true, false);
    }

    /**
     * Determines if runtime environment is currently running. This doesn't indicate if an
     * instruction is executed in this moment. Instead it indicates if the VM already started to
     * execute a loaded program.
     *
     * <p><b>Example:</b> If the first instruction of a program was already executed but a
     * breakpoint is reached and the execution flow paused, this method will still return
     * true.</p>
     *
     * @return Returns true if runtime environment is already running, otherwise false
     */

    public boolean isRunning() {

        return running;
    }

    /**
     * Determines if a program is already loaded to program memory.
     *
     * @return Returns true if a program is already loaded, otherwise false
     */

    public boolean isLoaded() {

        return loaded;
    }

    /**
     * Stimulates a selected pin of Port A. The given values represents the pin state, while
     * <i>0</i> represents <i>LOW</i> and no equals <i>0</i> represents <i>HIGH</i>.
     *
     * @param pin   Selected pin, range between 0 inclusive and 4 inclusive
     * @param isSet Value for selected pin, true indicates HIGH and false indicates LOW
     * @throws IllegalStateException    Thrown if selected pin is mapped as output
     * @throws IllegalArgumentException Thrown if selected pin doesn't exist
     */

    public void stimulatePortA(int pin, boolean isSet) throws IllegalArgumentException, IllegalStateException {

        // Check if pin exists

        if (4 < pin || 0 > pin) {

            throw new IllegalArgumentException("Selected pin doesn't exist");
        }

        // Check if pin is selected as input

        if (0x01 == (0x01 & (ram.get(RamMemory.SFR.TRISA) >> pin))) {

            if (isSet) {

                ram.set(RamMemory.SFR.PORTA, (byte) (ram.get(RamMemory.SFR.PORTA) | (0x01 << pin)));

            } else {

                ram.set(RamMemory.SFR.PORTA, (byte) (ram.get(RamMemory.SFR.PORTA) & ~(0x01 << pin)));
            }

        } else {

            throw new IllegalStateException("Selected pin is set as output pin");
        }

        LOGGER.debug(String.format("Sets pin %d of Port A to %s", pin, isSet ? "HIGH" : "LOW"));
    }

    /**
     * Stimulates a selected pin of Port B. The given values represents the pin state, while
     * <i>false</i> represents <i>LOW</i> and <i>true</i> represents <i>HIGH</i>.
     *
     * @param pin   Selected pin, range between 0 inclusive and 7 inclusive
     * @param isSet Value for selected pin, true indicates HIGH and false indicates LOW
     * @throws IllegalStateException    Thrown if selected pin is mapped as output
     * @throws IllegalArgumentException Thrown if selected pin doesn't exist
     */

    public void stimulatePortB(int pin, boolean isSet) throws IllegalArgumentException, IllegalStateException {

        // Check if pin exists

        if (7 < pin || 0 > pin) {

            throw new IllegalArgumentException("Selected pin doesn't exist");
        }

        // Check if pin is selected as input

        if (0x01 == (0x01 & (ram.get(RamMemory.SFR.TRISB) >> pin))) {

            if (isSet) {

                ram.set(RamMemory.SFR.PORTB, (byte) (ram.get(RamMemory.SFR.PORTB) | (0x01 << pin)));

            } else {

                ram.set(RamMemory.SFR.PORTB, (byte) (ram.get(RamMemory.SFR.PORTB) & ~(0x01 << pin)));
            }

        } else {

            throw new IllegalStateException("Selected pin is set as output pin");
        }

        LOGGER.debug(String.format("Sets pin %d of Port B to %s", pin, isSet ? "HIGH" : "LOW"));
    }
}
