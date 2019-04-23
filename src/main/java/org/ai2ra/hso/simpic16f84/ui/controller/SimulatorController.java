package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class SimulatorController {

    @FXML private void onQuitAction(ActionEvent event) {

        Platform.exit();
    }
}
