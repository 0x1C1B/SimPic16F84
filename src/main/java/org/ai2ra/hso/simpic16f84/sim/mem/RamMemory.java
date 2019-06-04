package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hardware specific RAM implementation for the Pic16F84 MCU. Like in the real
 * hardware implementation this structure works with two banks with size of 128 fields
 * for each.
 *
 * @author 0x1C1B
 * @param <T> Type of data that is stored, implicit this specifies the field width
 */

public class RamMemory<T> implements ObservableMemory<T> {

    public enum Bank {

        BANK_0, BANK_1
    }

    /**
     * Definition for the Special Function Registers (SFR) to prevent magic numbers.
     */

    public enum SFR {

        INDF(Bank.BANK_0, 0x00, true),
        TMR0(Bank.BANK_0, 0x01, false),
        PCL(Bank.BANK_0, 0x02, true),
        STATUS(Bank.BANK_0, 0x03, true),
        FSR(Bank.BANK_0, 0x04, true),
        PORTA(Bank.BANK_0, 0x05, false),
        PORTB(Bank.BANK_0, 0x06, false),
        EEDATA(Bank.BANK_0, 0x08, false),
        EEADR(Bank.BANK_0, 0x09, false),
        PCLATH(Bank.BANK_0, 0x0A, true),
        INTCON(Bank.BANK_0, 0x0B, true),
        OPTION(Bank.BANK_1, 0x01, false),
        TRISA(Bank.BANK_1, 0x05, false),
        TRISB(Bank.BANK_1, 0x06, false),
        EECON1(Bank.BANK_1, 0x08, false),
        EECON2(Bank.BANK_1, 0x09, false);

        private Bank bank;
        private int address;
        private boolean mapped;

        SFR(Bank bank, int address, boolean mapped) {

            this.bank = bank;
            this.address = address;
            this.mapped = mapped;
        }

        public int getAddress() {

            return address;
        }

        public Bank getBank() {

            return bank;
        }

        public boolean isMapped() {

            return mapped;
        }

        public static SFR valueOf(Bank bank, int address) {

            for (SFR sfr : values()) {

                if (sfr.isMapped() && address == sfr.address) {

                    return sfr; // Selected bank doesn't matter

                } else if (sfr.bank.equals(bank) && address == sfr.address) {

                    return sfr;
                }
            }

            throw new IllegalArgumentException("No such address found");
        }
    }

    public static final int BANK_SIZE;

    private T[] bank0;
    private T[] bank1;

    private PropertyChangeSupport changes;
    private ReadWriteLock lock;

    static {

        BANK_SIZE = 128;
    }

    @SuppressWarnings("unchecked")
    public RamMemory() {

        this.bank0 = (T[]) new Object[BANK_SIZE];
        this.bank1 = (T[]) new Object[BANK_SIZE];
        this.changes = new PropertyChangeSupport(this);
        this.lock = new ReentrantReadWriteLock();
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

    public T get(Bank bank, int address) {

        lock.readLock().lock();

        try {

            if(0 > address || BANK_SIZE <= address) {

                throw new MemoryIndexOutOfBoundsException("Address isn't implemented");

            }

            if(bank.equals(Bank.BANK_0)) {

                return bank0[address];

            } else {

                return bank1[address];
            }

        } finally {

            lock.readLock().unlock();
        }
    }

    public void set(Bank bank, int address, T value) {

        lock.writeLock().lock();

        try {

            if(0 > address || BANK_SIZE <= address) {

                throw new MemoryIndexOutOfBoundsException("Address isn't implemented");

            } else if(0x0C > address) {

                // Fill Special Function Registers and map them if required

                switch(address) {

                    case 0x00: {

                        break; // Indirect Address Register (not physically implemented)
                    }
                    case 0x02: // PCL
                    case 0x03: // STATUS
                    case 0x04: // FSR
                    case 0x0A: // PCLATH
                    case 0x0B: { // INTCON

                        // Handle mapped registers

                        changes.fireIndexedPropertyChange("bank0",
                                address, bank0[address], value);

                        changes.fireIndexedPropertyChange("bank1",
                                address, bank1[address], value);

                        bank0[address] = value;
                        bank1[address] = value;

                        break;
                    }
                    default: {

                        // Handle single location implemented registers

                        if(bank.equals(Bank.BANK_0)) {

                            changes.fireIndexedPropertyChange("bank0",
                                    address, bank0[address], value);

                            bank0[address] = value;

                        } else {

                            changes.fireIndexedPropertyChange("bank1",
                                    address, bank1[address], value);

                            bank1[address] = value;
                        }
                        break;
                    }
                }

            } else {

                changes.fireIndexedPropertyChange("bank0",
                        address, bank0[address], value);

                changes.fireIndexedPropertyChange("bank1",
                        address, bank1[address], value);

                if(bank.equals(Bank.BANK_0)) {

                    bank0[address] = value;
                    bank1[address] = value; // Mapped to second bank

                } else {

                    bank1[address] = value;
                    bank0[address] = value; // Mapped to second bank
                }
            }

        } finally {

            lock.writeLock().unlock();
        }
    }

    public void set(SFR sfr, T value) {

        set(sfr.getBank(), sfr.getAddress(), value);
    }

    public T get(SFR sfr) {

        return get(sfr.getBank(), sfr.getAddress());
    }
}
