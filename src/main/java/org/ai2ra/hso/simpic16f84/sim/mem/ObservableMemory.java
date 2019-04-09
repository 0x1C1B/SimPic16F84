package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;

/**
 * Interface allowing external modules watching changes and values inside of the
 * memory in read-only mode. This allows decoupled graphical representation of the memory
 * content in real-time.
 *
 * @author 0x1C1B
 * @see MemoryIndexOutOfBoundsException
 * @param <T> The type of data that is stored inside of the memory
 */

public interface ObservableMemory<T> {

    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the stored value inside of memory at a given address. If address
     * doesn't exist, meaning is out of bounds, an exception is thrown.
     *
     * @param address The memory address
     * @return Returns the stored value at the given address
     * @throws MemoryIndexOutOfBoundsException Thrown if address doesn't exists
     */

    T get(int address) throws MemoryIndexOutOfBoundsException;

    /**
     * Used for fetching the whole internal memory in form of an array.
     * <b>Warning:</b> For preventing manipulation and/or gaining read-only access,
     * a deep copy of the memory should returned.
     *
     * @return The internal memory represented in form of an array
     */

    T[] fetch();
}
