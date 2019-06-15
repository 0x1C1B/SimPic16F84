package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents the EEPROM (Electrically Erasable Programmable Read-Only Memory) structure.
 * In theory or of course also hardware implemented, this memory block allows persisting
 * data beyond MCU restarts. The current software implementation of this class <b>doesn't</b>
 * support such behaviour.
 *
 * @param <T> The type of data that is stored inside of memory
 * @author Freddy1096
 */

public class EepromMemory<T> implements ObservableMemory<T> {

    private T[] memory;
    private PropertyChangeSupport changes;
    private ReadWriteLock lock;

    @SuppressWarnings("unchecked")
    public EepromMemory(int size) {

        this.memory = (T[]) new Object[size];
        changes = new PropertyChangeSupport(this);
        lock = new ReentrantReadWriteLock();

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        lock.writeLock().lock();

        try {

            changes.addPropertyChangeListener(listener);

        }finally {

            lock.writeLock().unlock();
        }

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        lock.writeLock().lock();

        try {

            changes.removePropertyChangeListener(listener);

        }finally {

            lock.writeLock().unlock();

        }


    }

    @Override
    public T get(int address) throws MemoryIndexOutOfBoundsException {

        lock.readLock().lock();

        try {

            if (memory.length == 0 || address > memory.length || address < 0) {

                throw new MemoryIndexOutOfBoundsException();
            }

            return memory[address];

        }finally {

            lock.readLock().unlock();

        }
    }

    public void set(int address, T toSet) {

        lock.writeLock().lock();

        try {

            if (memory.length == 0 || address > memory.length || address < 0) {

                throw new MemoryIndexOutOfBoundsException();

            } else {
                T beforeSet = memory[address];
                this.memory[address] = toSet;
                changes.fireIndexedPropertyChange("memory",
                        address, beforeSet, toSet);

            }
        }finally {

            lock.writeLock().unlock();

        }
    }
}


