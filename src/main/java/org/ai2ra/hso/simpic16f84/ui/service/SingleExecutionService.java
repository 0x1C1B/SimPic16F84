package org.ai2ra.hso.simpic16f84.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ai2ra.hso.simpic16f84.sim.Pic16F84VM;

/**
 * Service for executing a single instruction. It's implementation causes that the next
 * instruction in memory is executed.
 *
 * @author 0x1C1B
 * @see Pic16F84VM
 */

public class SingleExecutionService extends Service<Integer> {

    /**
     * Simulator instance accessed by this service wrapper
     */
    private Pic16F84VM simulator;

    public SingleExecutionService() {

        setOnFailed(new ServiceErrorHandler()); // Register default error handler
    }

    public Pic16F84VM getSimulator() {

        return simulator;
    }

    public void setSimulator(Pic16F84VM simulator) {

        this.simulator = simulator;
    }

    @Override
    protected Task<Integer> createTask() {

        return new Task<Integer>() {

            @Override
            protected Integer call() throws Exception {

                return simulator.execute();
            }
        };
    }
}
