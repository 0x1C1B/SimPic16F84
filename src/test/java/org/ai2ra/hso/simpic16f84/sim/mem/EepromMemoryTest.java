package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EepromMemoryTest {

    private EepromMemory<Byte> eeprom;

    @Before
    public void setUp() throws Exception {

        this.eeprom = new EepromMemory<>(64);
    }

    @Test
    public void testSet(){

        eeprom.set(0, (byte) 1);
        eeprom.set(63, (byte) 5);
        eeprom.set(30, (byte) 6);

        assertEquals("A wrong value was set", 1, (byte) eeprom.get(0));
        assertEquals("A wrong value was set", 5, (byte) eeprom.get(63));
        assertEquals("A wrong value was set", 6, (byte) eeprom.get(30));
    }

    @Test(expected = MemoryIndexOutOfBoundsException.class) public void testInvalidSet() {

        eeprom.set(-1, (byte) 2);
    }

    @Test
    public void testGet() {

        eeprom.set(0, (byte) 1);
        eeprom.set(63, (byte) 5);
        eeprom.set(30, (byte) 6);

        assertEquals("A wrong value was returned", 1, (byte) eeprom.get(0));
        assertEquals("A wrong value was returned", 5, (byte) eeprom.get(63));
        assertEquals("A wrong value was returned", 6, (byte) eeprom.get(30));
    }

}