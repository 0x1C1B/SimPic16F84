package org.ai2ra.hso.simpic16f84.ui.service;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.ai2ra.hso.simpic16f84.ui.util.ApplicationDialog;

/**
 * General error handling utility for handling occurred exceptions of a service thread.
 *
 * @author 0x1C1B
 */

public class ServiceErrorHandler implements EventHandler<WorkerStateEvent> {

    @Override
    public void handle(WorkerStateEvent event) {

        Platform.runLater(() -> {

            ApplicationDialog.showError(event.getSource().getException());
            event.getSource().getException().printStackTrace(System.err);
        });
    }
}
