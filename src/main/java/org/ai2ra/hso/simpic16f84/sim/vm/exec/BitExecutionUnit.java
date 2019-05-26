package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.apache.log4j.Logger;

/**
 * Execution unit is responsible for executing bit operations. Originally this class
 * was and logical is part of the executor itself. Because of maintainability reasons
 * it is separated package-private.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see InstructionExecutor
 */

public class BitExecutionUnit {

    private static final Logger LOGGER;

    private InstructionExecutor executor;

    static {

        LOGGER = Logger.getLogger(InstructionExecutor.class);
    }

    BitExecutionUnit(InstructionExecutor executor) {

        this.executor = executor;
    }
}
