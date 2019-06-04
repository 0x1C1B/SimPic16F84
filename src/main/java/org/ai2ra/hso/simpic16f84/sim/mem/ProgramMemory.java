package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents the memory block that holds all instructions that are part of the
 * loaded program.
 *
 * @param <T> The type of data that is stored inside of memory
 * @author Freddy1096
 */

public class ProgramMemory <T> implements ObservableMemory<T> {

    private T[] memory;
    private PropertyChangeSupport changes;
    private ReadWriteLock lock;

    @SuppressWarnings("unchecked")
    public ProgramMemory(int size) {

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

    public void set(T toSet, int address) {

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
