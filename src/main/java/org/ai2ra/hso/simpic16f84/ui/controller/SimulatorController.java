package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

public class SimulatorController implements Initializable {

    @FXML private AnchorPane contentPane;
    private LstViewer lstViewer;

    public SimulatorController() {

        lstViewer = new LstViewer();
    }

    @Override public void initialize(URL location, ResourceBundle resources) {

        // Include custom component via code not markup

        AnchorPane.setTopAnchor(lstViewer, 40.0);
        AnchorPane.setLeftAnchor(lstViewer, 0.0);
        AnchorPane.setRightAnchor(lstViewer, 0.0);
        AnchorPane.setBottomAnchor(lstViewer, 0.0);
        contentPane.getChildren().add(lstViewer);
    }

    @FXML private void onQuitAction(ActionEvent event) {

        Platform.exit();
    }

    @FXML private void onOpenAction(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open LST File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("LST Files", "*.LST")
        );

        File file = fileChooser.showOpenDialog(null);

        if(null != file) {

            Task<String> task = new Task<String>() {

                @Override protected String call() throws Exception {

                    StringBuilder builder = new StringBuilder();

                    try(BufferedReader reader = new BufferedReader(new FileReader(file))) {

                        String line;

                        while(null != (line = reader.readLine())) {

                            builder.append(line).append(System.lineSeparator());
                        }
                    }

                    return builder.toString();
                }
            };

            task.setOnSucceeded((evt) -> {

                lstViewer.replaceText(task.getValue());
                lstViewer.moveTo(0, 0);
            });

            task.setOnFailed((evt) -> {

                task.getException().printStackTrace(System.err);
            });

            new Thread(task).start();
        }
    }

    @FXML private void onBreakpointAction(ActionEvent event) {

        lstViewer.toggleBreakpoint();
    }
}
