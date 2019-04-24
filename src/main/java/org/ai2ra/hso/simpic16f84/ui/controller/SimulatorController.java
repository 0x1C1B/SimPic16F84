package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulatorController implements Initializable {

    @FXML private CodeArea lstView;

    @Override public void initialize(URL location, ResourceBundle resources) {

        lstView.setParagraphGraphicFactory(LineNumberFactory.get(lstView));
    }

    @FXML private void onQuitAction(ActionEvent event) {

        Platform.exit();
    }
}
