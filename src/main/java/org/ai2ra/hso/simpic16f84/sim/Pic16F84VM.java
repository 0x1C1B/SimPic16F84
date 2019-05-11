package org.ai2ra.hso.simpic16f84.sim;

import org.ai2ra.hso.simpic16f84.sim.mem.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

/**
 * Runtime environment for the Pic16F84 simulator. This class holds all memory
 * blocks as well as the control flow. Supports property change listeners for
 * observing the current state, means the loaded and running status.
 *
 * @author 0x1C1B
 * @see InstructionExecutor
 * @see LstParser
 */

public class Pic16F84VM {

    private ProgramMemory<Integer> programMemory;
    private RamMemory<Integer> ram;
    private StackMemory<Integer> stack;
    private EepromMemory<Integer> eeprom;

    private InstructionExecutor executor;

    private boolean loaded; // Determines if a valid program was loaded
    private boolean running; // Determines if the execution is running

    private PropertyChangeSupport changes;

    /**
     * Initializes a usable state of the Pic16F84 runtime environment.
     */

    public Pic16F84VM() {

        this.programMemory = new ProgramMemory<>(1000);
        this.ram = new RamMemory<>();
        this.stack = new StackMemory<>(8);
        this.eeprom = new EepromMemory<>(64);

        this.executor = new InstructionExecutor(programMemory, ram, stack, eeprom);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Fetching program memory in a pseudo read-only state. Method is intended to use
     * for observing the memory.
     *
     * @return Returns the program memory in a pseudo read-only state
     */

    public ObservableMemory<Integer> getProgramMemory() {

        return programMemory;
    }

    /**
     * Fetching RAM in a pseudo read-only state. Method is intended to use
     * for observing the memory.
     *
     * @return Returns the RAM in a pseudo read-only state
     */

    public ObservableMemory<Integer> getRam() {

        return ram;
    }

    /**
     * Fetching stack memory in a pseudo read-only state. Method is intended to use
     * for observing the memory.
     *
     * @return Returns the stack memory in a pseudo read-only state
     */

    public ObservableMemory<Integer> getStack() {

        return stack;
    }

    /**
     * Fetching EEPROM in a pseudo read-only state. Method is intended to use
     * for observing the memory.
     *
     * @return Returns the EEPROM in a pseudo read-only state
     */

    public ObservableMemory<Integer> getEeprom() {

        return eeprom;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {

        changes.addPropertyChangeListener(listener);
    }

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

        int[] instructions = LstParser.parse(file); // Extract machine instructions

        // Load extracted machine instructions into program memory

        for (int address = 0; address < instructions.length; ++address) {

            programMemory.set(instructions[address], address);
        }

        loaded = true; // Set state to execution ready
        changes.firePropertyChange("loaded", false, true);
    }

    /**
     * Executes the next instruction cycle, basically just the next instruction.
     *
     * @throws IllegalStateException Thrown if no valid program was previously loaded
     * @see Pic16F84VM#load(File)
     * @return Returns the address of next instruction
     */

    public int nextStep() {

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
     * Determines if runtime environment is currently running.
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
}
