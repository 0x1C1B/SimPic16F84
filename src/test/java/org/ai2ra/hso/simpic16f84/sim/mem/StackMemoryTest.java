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
    }
}