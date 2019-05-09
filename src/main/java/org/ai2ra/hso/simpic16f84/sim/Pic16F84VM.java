package org.ai2ra.hso.simpic16f84.sim;

import org.ai2ra.hso.simpic16f84.sim.mem.*;

/**
 * Runtime environment for the Pic16F84 simulator. This class holds all memory
 * blocks as well as the control flow.
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

    /**
     * Initializes a usable state of the Pic16F84 runtime environment.
     */

    public Pic16F84VM() {

        this.programMemory = new ProgramMemory<>(1000);
        this.ram = new RamMemory<>();
        this.stack = new StackMemory<>(8);
        this.eeprom = new EepromMemory<>(64);

        this.executor = new InstructionExecutor(programMemory, ram, stack, eeprom);
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
}
