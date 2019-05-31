package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RamMemoryTest {

    private RamMemory<Byte> ram;

    @Before
    public void setUp() throws Exception {

        ram = new RamMemory<>();
    }

    @Test public void writeTest() {

        ram.set(RamMemory.Bank.BANK_0, 22, (byte) 33);
        assertEquals((byte) ram.get(RamMemory.Bank.BANK_0, 22), 33);
    }

    @Test public void mappedWriteTest() {

        ram.set(RamMemory.Bank.BANK_0, 4, (byte) 11);
        assertEquals((byte) ram.get(RamMemory.Bank.BANK_0, 4), 11);
        assertEquals((byte) ram.get(RamMemory.Bank.BANK_1, 4), 11);
    }

    @Test public void writeSFRTest() {

        ram.set(RamMemory.SFR.PORTA, (byte) 3);
        assertEquals((byte) ram.get(RamMemory.SFR.PORTA), 3);
        assertEquals((byte) ram.get(RamMemory.Bank.BANK_0, 5), 3);
    }
}