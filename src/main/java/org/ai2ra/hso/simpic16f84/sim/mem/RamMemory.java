package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;

public class RamMemory<T> implements ObservableMemory<T> {

    private T[] bank0;
    private T[] bank1;

    private PropertyChangeSupport changes;
    private ReadWriteLock lock;

    @SuppressWarnings("unchecked")
    public RamMemory(int bankSize) {

        this.bank0 = (T[]) new Object[bankSize];
        this.bank1 = (T[]) new Object[bankSize];
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

    /**
     * Address space of bank0 and bank1 is combined for addressing inside this method. This
     * means that address space is from 0 to <code>bank0.length + bank1.length - 1</code>
     * instead of 0 to <code>bank.length - 1</code> per bank. Switching the bank isn't
     * necessary, the address spaces of the banks are merged inside of this method.
     *
     * @param address The memory address
     * @return The value stored at the given address
     * @throws MemoryIndexOutOfBoundsException Thrown if address is outside of memory range
     */

    @Override
    public T get(int address) throws MemoryIndexOutOfBoundsException {

        lock.readLock().lock();

        try {

            if(0 > address || bank0.length + bank1.length <= address) {

                throw new MemoryIndexOutOfBoundsException("Stack contains only eight levels");
            }

            if(bank0.length > address) {

                return bank0[address];

            } else {

                return bank1[address];
            }

        } finally {

            lock.readLock().unlock();
        }
    }

    /**
     * Used for fetching the memory as connected structure. Bank0 and bank1 are merged to an
     * single array. This also means that the mapped memory locations, meaning the
     * general purpose registers (GPR), are existing twice.
     *
     * @return The combined memory consisting out of bank0 and bank1
     */

    @Override
    public T[] fetch() {

        T[] memory = Arrays.copyOf(bank0, bank0.length + bank1.length);
        System.arraycopy(bank1, 0, memory, bank0.length, bank1.length);
        return memory;
    }
}
