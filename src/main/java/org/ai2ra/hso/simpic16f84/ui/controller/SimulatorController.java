package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.ai2ra.hso.simpic16f84.ui.util.BreakpointFactory;
import org.ai2ra.hso.simpic16f84.ui.util.SyntaxHighlighting;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.TwoDimensional;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.function.IntFunction;

import static org.fxmisc.wellbehaved.event.EventPattern.*;

public class SimulatorController implements Initializable {

    @FXML private CodeArea lstView;
    private ObservableSet<Integer> breakpoints;

    public SimulatorController() {

        breakpoints = FXCollections.observableSet();
    }

    @Override public void initialize(URL location, ResourceBundle resources) {

        IntFunction<Node> numberFactory = LineNumberFactory.get(lstView);
        IntFunction<Node> breakpointFactory = new BreakpointFactory(breakpoints);

        IntFunction<Node> graphicFactory = line -> {

            HBox hbox = new HBox(numberFactory.apply(line),
                    breakpointFactory.apply(line));

            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };

        lstView.setParagraphGraphicFactory(graphicFactory);

        // Enable syntax highlighting, updated all 500 ms after typing is stopped/ text is set

        lstView.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(change -> lstView.setStyleSpans(0, SyntaxHighlighting.compute(lstView.getText())));

        // Enable key listeners for setting breakpoints

        InputMap<Event> inputMap = InputMap.consume(
                keyPressed(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN)),
                (ignore) -> toggleBreakpoint());

        Nodes.addInputMap(lstView, inputMap);
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

                lstView.replaceText(task.getValue());
                lstView.moveTo(0, 0);
            });

            task.setOnFailed((evt) -> {

                task.getException().printStackTrace(System.err);
            });

            new Thread(task).start();
        }
    }

    @FXML private void onBreakpointAction(ActionEvent event) {

        toggleBreakpoint();
    }

    /**
     * Toggles a breakpoint for the currently selected line inside of
     * the LST view. With selection, the current caret position (line)
     * is meant. If breakpoint is already set, it is removed otherwise
     * it is set.
     *
     * @see #lstView
     */

    private void toggleBreakpoint() {

        int line = lstView.offsetToPosition(
                lstView.getCaretPosition(), TwoDimensional.Bias.Forward).getMajor();

        if(breakpoints.contains(line)) {

            breakpoints.remove(line);

        } else {

            breakpoints.add(line);
        }
    }
}
