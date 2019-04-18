package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProgramMemoryTest {

    ProgramMemory testee = new ProgramMemory(64);


    @Before
    public void testSetUp(){

        testee.set((byte) 1,0);
        testee.set((byte) 5, 63);
        testee.set((byte) 6, 30);

    }

    @Test
    public void testSet(){


        assertEquals("A wrong number was returned", 1, (byte)testee.get(0));
        assertEquals("A wrong number was returned", 5, (byte)testee.get(63));
        assertEquals("A wrong number was returned", 6, (byte)testee.get(30));

    }

    @Test(expected = MemoryIndexOutOfBoundsException.class) public void testInvalidSet() {

        testee.set((byte) 2, -1);
        testee.set((byte) 4, 64);
        testee.get(-1);
        testee.get(64);
    }

    @Test
    public void testGet() {

        assertEquals("A wrong number was returned", 1, (byte)testee.get(0));
        assertEquals("A wrong number was returned", 5, (byte)testee.get(63));
        assertEquals("A wrong number was returned", 6, (byte)testee.get(30));

    }

    @Test
    public void testReset() {

        testee.reset();

        assertEquals("Memory did not reset properly", null, testee.get(0));

    }
}