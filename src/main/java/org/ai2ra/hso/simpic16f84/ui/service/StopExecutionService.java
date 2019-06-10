package org.ai2ra.hso.simpic16f84.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ai2ra.hso.simpic16f84.sim.Pic16F84VM;

/**
 * Service stops the current execution flow.
 *
 * @author 0x1C1B
 * @see Pic16F84VM
 */

public class StopExecutionService extends Service<Void> {

    /**
     * Simulator instance accessed by this service wrapper
     */
    private Pic16F84VM simulator;

    public StopExecutionService() {

        setOnFailed(new ServiceErrorHandler()); // Register default error handler
    }

    public Pic16F84VM getSimulator() {

        return simulator;
    }

    public void setSimulator(Pic16F84VM simulator) {

        this.simulator = simulator;
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                simulator.stop();
                return null;
            }
        };
    }
}
