package org.ai2ra.hso.simpic16f84.sim.vm;

import java.beans.PropertyChangeListener;

/**
 * Allows external modules to observe the execution flow by watching important
 * registers and values.
 *
 * @author 0x1C1B
 */

public interface ObservableExecution {

    /**
     * Used for fetching the current state of the working register (accumulator).
     *
     * @return Returns the current content of the working register
     */

    Integer getWorkingRegister();

    /**
     * The instruction register contains the next executable instruction before it's
     * execution.
     *
     * @return Returns the content of the insrtuction register
     */

    Integer getInstructionRegister();

    /**
     * Allows read-only access to the program counter (instruction pointer).
     *
     * @return Returns the current address the instruction pointer is pointing to
     */

    Integer getProgramCounter();

    /**
     * Adds a change listener <b>only</b> for observing the executor's state. This pattern
     * is specially intended to use for internal registers.
     *
     * @param listener The listener that should be registered
     */

    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a change listener <b>only</b> from the change support of the executor.
     *
     * @param listener The listener that should be removed
     */

    void removePropertyChangeListener(PropertyChangeListener listener);
}
