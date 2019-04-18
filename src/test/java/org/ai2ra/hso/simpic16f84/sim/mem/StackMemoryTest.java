package org.ai2ra.hso.simpic16f84.sim.mem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackMemoryTest {

    private StackMemory<Integer> stack;

    @Before public void setUp() throws Exception {

        stack = new StackMemory<>(8);
    }

    @Test public void pushTest() {

        stack.addPropertyChangeListener(event -> assertEquals((int) event.getNewValue(), 5));
        stack.push(5);

        assertFalse(stack.isEmpty());
        assertFalse(stack.isFull());
    }

    @Test public void popTest() {

        stack.push(11);

        stack.addPropertyChangeListener(event -> assertEquals((int) event.getNewValue(), 0));
        assertEquals((int) stack.pop(), 11);
    }

    @Test public void isEmptyTest() {

        assertTrue(stack.isEmpty());
    }

    @Test(expected = MemoryIndexOutOfBoundsException.class) public void getInvalidTest() {

        stack.get(-5);
    }
}