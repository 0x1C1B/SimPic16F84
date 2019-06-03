package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProgramMemoryTest {

    ProgramMemory<Short> testee = new <Short>ProgramMemory(64);


    @Before
    public void testSetUp(){

        testee.set((short) 1,0);
        testee.set((short) 5, 63);
        testee.set((short) 6, 30);

    }

    @Test
    public void testSet(){


        assertEquals("A wrong number was returned", 1, (short)testee.get(0));
        assertEquals("A wrong number was returned", 5, (short)testee.get(63));
        assertEquals("A wrong number was returned", 6, (short)testee.get(30));

    }

    @Test(expected = MemoryIndexOutOfBoundsException.class) public void testInvalidSet() {

        testee.set((short) 2, -1);
        testee.set((short) 4, 64);
        testee.get(-1);
        testee.get(64);
    }

    @Test
    public void testGet() {

        assertEquals("A wrong number was returned", 1, (short)testee.get(0));
        assertEquals("A wrong number was returned", 5, (short)testee.get(63));
        assertEquals("A wrong number was returned", 6, (short)testee.get(30));

    }
}