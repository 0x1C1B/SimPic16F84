package org.ai2ra.hso.simpic16f84.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ai2ra.hso.simpic16f84.sim.Pic16F84VM;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;

/**
 * Continues the execution until a breakpoint is reached. This service is executed
 * in a separate thread.
 *
 * @author 0x1C1B
 * @see Pic16F84VM
 * @see LstViewer
 */

public class RunExecutionService extends Service<Integer> {

    private Pic16F84VM simulator;
    private LstViewer lstViewer;

    public Pic16F84VM getSimulator() {

        return simulator;
    }

    public void setSimulator(Pic16F84VM simulator) {

        this.simulator = simulator;
    }

    public LstViewer getLstViewer() {

        return lstViewer;
    }

    public void setLstViewer(LstViewer lstViewer) {

        this.lstViewer = lstViewer;
    }

    @Override
    protected Task<Integer> createTask() {

        return new Task<Integer>() {

            @Override
            protected Integer call() throws Exception {

                int address;

                do {

                    address = simulator.nextStep();

                    updateValue(address);
                    Thread.sleep(500); // Give the UI time for rendering updates

                    // Continues until a breakpoint is reached

                    if (lstViewer.getBreakpoints()
                            .contains(lstViewer.addressToLineNumber(address))) {

                        return address;
                    }

                } while (simulator.isRunning()); // Continues until stop is called

                return address;
            }
        };
    }
}
