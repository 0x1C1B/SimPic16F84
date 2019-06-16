package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProgramMemoryTest {

    private ProgramMemory<Short> memory;

    @Before
    public void setUp() throws Exception {

        this.memory = new ProgramMemory<>(64);
    }

    @Test
    public void testSet(){

        memory.set(0, (short) 1);
        assertEquals("A wrong number was set", 1, (short) memory.get(0));
    }

    @Test(expected = MemoryIndexOutOfBoundsException.class) public void testInvalidSet() {

        memory.set(-1, (short) 2);
    }

    @Test
    public void testGet() {

        memory.set(30, (short) 6);
        assertEquals("A wrong value was returned", 6, (short) memory.get(30));
    }
}