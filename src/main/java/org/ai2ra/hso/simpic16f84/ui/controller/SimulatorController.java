package org.ai2ra.hso.simpic16f84.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanPropertyBuilder;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.ai2ra.hso.simpic16f84.sim.Pic16F84VM;
import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;
import org.ai2ra.hso.simpic16f84.ui.model.GeneralPurposeRegister;
import org.ai2ra.hso.simpic16f84.ui.model.SpecialFunctionRegister;
import org.ai2ra.hso.simpic16f84.ui.model.StatusRegister;
import org.ai2ra.hso.simpic16f84.ui.util.TextAreaAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
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

    @FXML TableView<StatusRegister> statusRegister;
    @FXML TableColumn<StatusRegister, Integer> irpBit;
    @FXML TableColumn<StatusRegister, Integer> rp1Bit;
    @FXML TableColumn<StatusRegister, Integer> rp0Bit;
    @FXML TableColumn<StatusRegister, Integer> toBit;
    @FXML TableColumn<StatusRegister, Integer> pdBit;
    @FXML TableColumn<StatusRegister, Integer> zBit;
    @FXML TableColumn<StatusRegister, Integer> dcBit;
    @FXML TableColumn<StatusRegister, Integer> cBit;

    // Special Function Registers representation

    @FXML TableView<SpecialFunctionRegister> specialRegisters;
    @FXML TableColumn<SpecialFunctionRegister, String> sfrName;
    @FXML TableColumn<SpecialFunctionRegister, String> sfrValue;

    // General Purpose Registers representation

    @FXML TableView<GeneralPurposeRegister> generalRegisters;
    @FXML TableColumn<GeneralPurposeRegister, String> gprAddress;
    @FXML TableColumn<GeneralPurposeRegister, String> gprValue;
    @FXML TableColumn<GeneralPurposeRegister, GeneralPurposeRegister> gprOptions;
    @FXML Spinner<Integer> addressField;

    // Address stack components

    @FXML ListView<String> addressStack;

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
        simulator.getStack().addPropertyChangeListener(new StackMemoryChangeListener());
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

        irpBit.setCellValueFactory(new PropertyValueFactory<>("irpFlag"));
        rp1Bit.setCellValueFactory(new PropertyValueFactory<>("rp1Flag"));
        rp0Bit.setCellValueFactory(new PropertyValueFactory<>("rp0Flag"));
        toBit.setCellValueFactory(new PropertyValueFactory<>("toFlag"));
        pdBit.setCellValueFactory(new PropertyValueFactory<>("pdFlag"));
        zBit.setCellValueFactory(new PropertyValueFactory<>("zeroFlag"));
        dcBit.setCellValueFactory(new PropertyValueFactory<>("digitCarryFlag"));
        cBit.setCellValueFactory(new PropertyValueFactory<>("carryFlag"));

        // Setup Special Function Register table view

        sfrName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Use custom factory for printing as hex string in prefix format
        sfrValue.setCellValueFactory(param -> new SimpleStringProperty(String.format("0x%02X", param.getValue().getValue())));

        // Setup General Purpose Register section

        SpinnerValueFactory<Integer> addressFactory = new SpinnerValueFactory.
                IntegerSpinnerValueFactory(0x0C, 0x7F, 0x0C);

        addressFactory.setConverter(new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {

                return String.format("0x%02X", object);
            }

            @Override
            public Integer fromString(String string) {

                return Integer.decode(string);
            }
        });

        addressField.setValueFactory(addressFactory);

        // Use custom factory for printing as hex string in prefix format
        gprAddress.setCellValueFactory(param -> new SimpleStringProperty(String.format("0x%02X", param.getValue().getAddress())));
        gprValue.setCellValueFactory(param -> new SimpleStringProperty(String.format("0x%02X", param.getValue().getValue())));

        gprOptions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        gprOptions.setCellFactory(param -> new TableCell<GeneralPurposeRegister, GeneralPurposeRegister>() {

            private Button delete = new Button();

            {
                delete.setGraphic(new FontIcon("fas-trash"));
                delete.setStyle("-fx-background-color: transparent;");
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(GeneralPurposeRegister item, boolean empty) {

                super.updateItem(item, empty);

                if (null == item) {

                    setGraphic(null);
                    return;
                }

                setGraphic(delete);
                delete.setOnAction(event -> getTableView().getItems().remove(item));
            }
        });
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

    @FXML
    private void onObserveRegisterAction(ActionEvent event) {

        int address = addressField.getValue();
        int value = null == simulator.getRam().get(address) ? 0 : simulator.getRam().get(address);

        // Check if entry already exists

        FilteredList<GeneralPurposeRegister> filtered = generalRegisters.getItems()
                .filtered(register -> address == register.getAddress());

        if (filtered.isEmpty()) {

            // Add new observer if it doesn't exist

            GeneralPurposeRegister register = new GeneralPurposeRegister();

            register.setAddress(address);
            register.setValue(value);

            generalRegisters.getItems().add(register);
        }
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
                    StatusRegister status = new StatusRegister();

                    // Disassemble STATUS register value in single bits

                    status.setIrpFlag((value >> 7) & 1);
                    status.setRp1Flag((value >> 6) & 1);
                    status.setRp0Flag((value >> 5) & 1);
                    status.setToFlag((value >> 4) & 1);
                    status.setPdFlag((value >> 3) & 1);
                    status.setZeroFlag((value >> 2) & 1);
                    status.setDigitCarryFlag((value >> 1) & 1);
                    status.setCarryFlag(value & 1);

                    Platform.runLater(() -> statusRegister.getItems().setAll(status));

                } else if (0x0C > ((IndexedPropertyChangeEvent) event).getIndex()) {

                    int address = ((IndexedPropertyChangeEvent) event).getIndex();
                    int value = (int) event.getNewValue(); // Value of the SFR
                    RamMemory.Bank bank = "bank0".equals(event.getPropertyName()) ?
                            RamMemory.Bank.BANK_0 :
                            RamMemory.Bank.BANK_1;

                    RamMemory.SFR sfr = RamMemory.SFR.valueOf(bank, address);

                    Platform.runLater(() -> {

                        // Check if entry already exists

                        FilteredList<SpecialFunctionRegister> filtered = specialRegisters.getItems()
                                .filtered(register -> register.getName().equals(sfr.name()));

                        if (filtered.isEmpty()) {

                            // Add new row if it doesn't exist

                            SpecialFunctionRegister register = new SpecialFunctionRegister();

                            register.setName(sfr.name());
                            register.setValue(value);

                            specialRegisters.getItems().add(register);

                        } else { // Entry exists, just update the value

                            // Only one match should exist, just uses the first one

                            filtered.get(0).setValue(value);
                        }
                    });

                } else if (0x0C <= ((IndexedPropertyChangeEvent) event).getIndex()) {

                    // Update just observed General Purpose Registers

                    int address = ((IndexedPropertyChangeEvent) event).getIndex();

                    FilteredList<GeneralPurposeRegister> filtered = generalRegisters.getItems()
                            .filtered(register -> address == register.getAddress());

                    if (!filtered.isEmpty()) {

                        // Only one match should exist, just uses the first one

                        filtered.get(0).setValue((int) event.getNewValue());
                    }
                }
            }
        }
    }

    /**
     * Responsible for handling memory changes inside of the stack memory
     * structure. This class updates the user interface when changes are
     * received.
     *
     * @author 0x1C1B
     * @see PropertyChangeListener
     */

    private class StackMemoryChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {

            Platform.runLater(() -> {

                if (null == event.getNewValue()) { // Element was removed

                    addressStack.getItems().remove(0); // Remove element on top of list

                } else {

                    // Add element to top of list

                    addressStack.getItems().add(0, String.format("0x%04X", (int) event.getNewValue()));
                }
            });
        }
    }
}
