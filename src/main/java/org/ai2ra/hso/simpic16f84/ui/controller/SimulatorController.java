package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.ai2ra.hso.simpic16f84.sim.Pic16F84VM;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;
import org.ai2ra.hso.simpic16f84.ui.util.TextAreaAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controls the interface of the simulator scene. Every GUI related listener
 * or task is registered inside this class.
 *
 * @author 0x1C1B
 */

public class SimulatorController implements Initializable {

    @FXML private AnchorPane contentPane;
    @FXML private TextArea logViewer;
    @FXML private ToggleGroup logLevel;

    private LstViewer lstViewer;

    // Toolbar and Menus

    @FXML private Button nextStepTool;
    @FXML private Button runTool;
    @FXML private Button stopTool;

    @FXML private MenuItem nextStepOption;
    @FXML private MenuItem runOption;
    @FXML private MenuItem stopOption;

    // STATUS register representation

    @FXML TableView<List<Integer>> statusRegister;
    @FXML TableColumn<List<Integer>, String> irpBit;
    @FXML TableColumn<List<Integer>, String> rp1Bit;
    @FXML TableColumn<List<Integer>, String> rp0Bit;
    @FXML TableColumn<List<Integer>, String> toBit;
    @FXML TableColumn<List<Integer>, String> pdBit;
    @FXML TableColumn<List<Integer>, String> zBit;
    @FXML TableColumn<List<Integer>, String> dcBit;
    @FXML TableColumn<List<Integer>, String> cBit;

    // Simulator related utilities

    private Pic16F84VM simulator;
    private ReadOnlyBooleanProperty runningProperty;
    private ReadOnlyBooleanProperty loadedProperty;
    private BooleanProperty executingProperty;

    public SimulatorController() {

        lstViewer = new LstViewer();
        simulator = new Pic16F84VM();

        // Allow property binding to the simulator state

        try {

            loadedProperty = ReadOnlyJavaBeanBooleanPropertyBuilder
                    .create()
                    .bean(simulator)
                    .name("loaded")
                    .getter("isLoaded")
                    .build();

            runningProperty = ReadOnlyJavaBeanBooleanPropertyBuilder
                    .create()
                    .bean(simulator)
                    .name("running")
                    .getter("isRunning")
                    .build();

        } catch (NoSuchMethodException exc) {

            exc.printStackTrace(System.err);
        }

        executingProperty = new SimpleBooleanProperty();

        // Register memory change listeners

        simulator.getRam().addPropertyChangeListener(new RamMemoryChangeListener());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Include custom component via code not markup

        AnchorPane.setTopAnchor(lstViewer, 40.0);
        AnchorPane.setLeftAnchor(lstViewer, 0.0);
        AnchorPane.setRightAnchor(lstViewer, 0.0);
        AnchorPane.setBottomAnchor(lstViewer, 0.0);
        contentPane.getChildren().add(lstViewer);

        // Redirect log stream to log viewer component

        TextAreaAppender.setTextArea(logViewer);

        // Allow change of log level

        logLevel.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {

            if (null != logLevel.getSelectedToggle()) {

                RadioMenuItem option = (RadioMenuItem) logLevel.getSelectedToggle();

                switch (option.getText()) {

                    case "Debug": {

                        Logger.getRootLogger().setLevel(Level.DEBUG);
                        break;
                    }
                    case "Warn": {

                        Logger.getRootLogger().setLevel(Level.WARN);
                        break;
                    }
                    case "Error": {

                        Logger.getRootLogger().setLevel(Level.ERROR);
                        break;
                    }
                    case "Info":
                    default: {

                        Logger.getRootLogger().setLevel(Level.INFO);
                        break;
                    }
                }
            }
        });

        // Enable/Disable tools dynamically until its loaded/running

        stopTool.disableProperty().bind(Bindings.or(
                loadedProperty.not(), runningProperty.not()));

        stopOption.disableProperty().bind(Bindings.or(
                loadedProperty.not(), runningProperty.not()));

        runTool.disableProperty().bind(Bindings.or(
                loadedProperty.not(), executingProperty));

        runOption.disableProperty().bind(Bindings.or(
                loadedProperty.not(), executingProperty));

        nextStepTool.disableProperty().bind(Bindings.or(
                loadedProperty.not(), executingProperty));

        nextStepOption.disableProperty().bind(Bindings.or(
                loadedProperty.not(), executingProperty));

        // Setup STATUS register table view

        irpBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(7))));
        rp1Bit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(6))));
        rp0Bit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(5))));
        toBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(4))));
        pdBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(3))));
        zBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(2))));
        dcBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(1))));
        cBit.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(0))));
    }

    @FXML
    private void onQuitAction(ActionEvent event) {

        Platform.exit();
    }

    @FXML
    private void onOpenAction(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open LST File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("LST Files", "*.LST")
        );

        File file = fileChooser.showOpenDialog(null);

        if (null != file) {

            Task<String> task = new Task<String>() {

                @Override
                protected String call() throws Exception {

                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                        String line;

                        while (null != (line = reader.readLine())) {

                            builder.append(line).append(System.lineSeparator());
                        }
                    }

                    simulator.load(file); // Loads file also into simulator

                    return builder.toString();
                }
            };

            task.setOnSucceeded((evt) -> {

                lstViewer.replaceText(task.getValue());
                lstViewer.moveTo(0, 0);
            });

            task.setOnFailed((evt) -> {

                System.out.println("failed");
                task.getException().printStackTrace(System.err);
            });

            new Thread(task).start();
        }
    }

    @FXML
    private void onBreakpointAction(ActionEvent event) {

        lstViewer.toggleBreakpoint();
    }

    @FXML
    private void onNextStepAction(ActionEvent event) {

        executingProperty.set(true);

        Task<Integer> task = new Task<Integer>() {

            @Override
            protected Integer call() throws Exception {

                return simulator.nextStep();
            }
        };

        task.setOnSucceeded((evt) -> {

            executingProperty.set(false);
            lstViewer.setIndicator(lstViewer.addressToLineNumber(task.getValue()));
        });

        task.setOnFailed((evt) -> {

            executingProperty.set(false);
            task.getException().printStackTrace(System.err);
        });

        new Thread(task).start();
    }

    @FXML
    private void onStopAction(ActionEvent event) {

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                simulator.stop();
                return null;
            }
        };

        task.setOnSucceeded((evt -> {

            executingProperty.set(false);
            lstViewer.setIndicator(lstViewer.addressToLineNumber(0x00));
        }));

        task.setOnFailed((evt) -> {

            executingProperty.set(false);
            task.getException().printStackTrace(System.err);
        });

        new Thread(task).start();
    }

    @FXML
    private void onRunAction(ActionEvent event) {

        executingProperty.set(true);

        Task<Integer> task = new Task<Integer>() {

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

        task.valueProperty().addListener((observable, prevAddress, address) -> lstViewer.setIndicator(lstViewer.addressToLineNumber(address)));

        task.setOnSucceeded((evt) -> executingProperty.set(false));

        task.setOnFailed((evt) -> {

            executingProperty.set(false);
            task.getException().printStackTrace(System.err);
        });

        new Thread(task).start();
    }

    /**
     * Responsible for handling memory changes inside of the RAM memory
     * structure. This class updates the user interface when changes are
     * received.
     *
     * @author 0x1C1B
     * @see PropertyChangeListener
     */

    private class RamMemoryChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {

            if (event instanceof IndexedPropertyChangeEvent) {

                // Check which register/location changed

                if (0x03 == ((IndexedPropertyChangeEvent) event).getIndex()) {

                    int value = (int) event.getNewValue(); // Value of STATUS register
                    ObservableList<List<Integer>> data = FXCollections.observableArrayList();
                    List<Integer> status = new ArrayList<>();

                    // Disassemble STATUS register value in single bits

                    for (int bit = 0; bit < 8; ++bit) {

                        status.add(bit, (value >> bit) & 1);
                    }

                    data.clear();
                    data.add(status);
                    statusRegister.setItems(data);
                }
            }
        }
    }
}
