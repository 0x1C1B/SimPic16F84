package org.ai2ra.hso.simpic16f84.sim.mem;

/**
 * Thrown to indicate that an address/index of some memory data structure is out of range.
 *
 * @author 0x1C1B
 */

public class MemoryIndexOutOfBoundsException extends IndexOutOfBoundsException {

    public MemoryIndexOutOfBoundsException() {

        super();
    }

    public MemoryIndexOutOfBoundsException(String message) {

        super(message);
    }
}
