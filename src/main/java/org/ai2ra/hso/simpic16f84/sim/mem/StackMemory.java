package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Generic limited stack implementation, primarily written for storing return
 * addresses on jumps/function calls.
 *
 * @author 0x1C1B
 * @param <T> The type of data that is stored inside of stack
 */

public class StackMemory<T> implements ObservableMemory<T> {

    private T[] memory;
    private int pointer;
    private PropertyChangeSupport changes;
    private ReadWriteLock lock;

    @SuppressWarnings("unchecked")
    public StackMemory(int size) {

        this.memory = (T[]) new Object[size];
        this.pointer = -1;
        this.changes = new PropertyChangeSupport(this);
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        lock.writeLock().lock();

        try {

            changes.addPropertyChangeListener(listener);

        } finally {

            lock.writeLock().unlock();
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        lock.writeLock().lock();

        try {

            changes.removePropertyChangeListener(listener);

        } finally {

            lock.writeLock().unlock();
        }
    }

    /**
     * @param address The memory address
     * @return Returns the data stored at the given address
     * @throws MemoryIndexOutOfBoundsException Thrown if address violates the stack bounds
     */

    @Override
    public T get(int address) throws MemoryIndexOutOfBoundsException {

        lock.readLock().lock();

        try {

            if(0 > address || memory.length <= address) {

                throw new MemoryIndexOutOfBoundsException("Stack contains only eight levels");
            }

            return memory[address];

        } finally {

            lock.readLock().unlock();
        }
    }

    /**
     * Pushes a new element to the top of stack. If stack is already full an exception
     * is thrown. On change a property change event for the <code>memory</code> property
     * is fired.
     *
     * @param value The new element that should be pushed
     * @throws MemoryIndexOutOfBoundsException Thrown if the stack is already full
     */

    public void push(T value) throws MemoryIndexOutOfBoundsException {

        lock.writeLock().lock();

        try {

            if(isFull()) {

                throw new MemoryIndexOutOfBoundsException("Stack overflow detected, stack is full");
            }

            memory[++pointer] = value;

            changes.fireIndexedPropertyChange("memory",
                    pointer, null, value);

        } finally {

            lock.writeLock().unlock();
        }
    }

    /**
     * Pops the latest element from top of stack. If stack is empty an exception
     * is thrown. On change a property change event for the <code>memory</code> property
     * is fired.
     *
     * @return Returns the element from top of stack
     * @throws MemoryIndexOutOfBoundsException Thrown if stack is empty
     */

    public T pop() throws MemoryIndexOutOfBoundsException {

        lock.writeLock().lock();

        try {

            if(isEmpty()) {

                throw new MemoryIndexOutOfBoundsException("Stack underflow detected, stack is empty");
            }

            changes.fireIndexedPropertyChange("memory",
                    pointer, memory[pointer], null);

            return memory[pointer--];

        } finally {

            lock.writeLock().unlock();
        }
    }

    /**
     * Determines the latest element from top of stack <b>without</b> removing them.
     * If stack is empty an exception is thrown.
     *
     * @return Returns the element from top of stack
     * @throws MemoryIndexOutOfBoundsException Thrown if stack is empty
     */

    public T top() throws MemoryIndexOutOfBoundsException {

        lock.readLock().lock();

        try {

            if(isEmpty()) {

                throw new MemoryIndexOutOfBoundsException("Stack underflow detected, stack is empty");
            }

            return memory[pointer];

        } finally {

            lock.readLock().unlock();
        }
    }

    /**
     * Determines if the limited stack is full. For preventing a stack overflow
     * this method could be used for checking the bounds.
     *
     * @return Returns true if it's full, otherwise false
     */

    public boolean isFull() {

        lock.readLock().lock();

        try {

            return memory.length - 1 == pointer;

        } finally {

            lock.readLock().unlock();
        }
    }

    /**
     * Determines if the limited stack is empty. For preventing a stack underflow,
     * meaning no elements are available, this method could be used for checking the
     * current fill level.
     *
     * @return Returns true if it's empty, otherwise false
     */

    public boolean isEmpty() {

        lock.readLock().lock();

        try {

            return -1 == pointer;

        } finally {

            lock.readLock().unlock();
        }
    }
}
