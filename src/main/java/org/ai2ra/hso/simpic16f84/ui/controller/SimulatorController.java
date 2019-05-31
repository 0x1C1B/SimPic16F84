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
import org.ai2ra.hso.simpic16f84.ui.service.*;
import org.ai2ra.hso.simpic16f84.ui.util.ApplicationDialog;
import org.ai2ra.hso.simpic16f84.ui.util.TextAreaAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import java.beans.EventHandler;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Optional;
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

    // Special register components

    @FXML TextField workingRegister;
    @FXML TextField instructionRegister;

    // I/O Pin representation

    @FXML CheckBox ra0;
    @FXML CheckBox ra1;
    @FXML CheckBox ra2;
    @FXML CheckBox ra3;
    @FXML CheckBox ra4;

    @FXML CheckBox rb0;
    @FXML CheckBox rb1;
    @FXML CheckBox rb2;
    @FXML CheckBox rb3;
    @FXML CheckBox rb4;
    @FXML CheckBox rb5;
    @FXML CheckBox rb6;
    @FXML CheckBox rb7;

    // Simulator related utilities

    private Pic16F84VM simulator;
    private ReadOnlyBooleanProperty runningProperty;
    private ReadOnlyBooleanProperty loadedProperty;
    private BooleanProperty executingProperty;

    // Simulator related services

    private LstReaderService lstReaderService;
    private NextStepService nextStepService;
    private StopExecutionService stopExecutionService;
    private RunExecutionService runExecutionService;

    public SimulatorController() {

        lstViewer = new LstViewer();

        initializeSimulator();
        initializeServices();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Include custom component via code not markup

        AnchorPane.setTopAnchor(lstViewer, 40.0);
        AnchorPane.setLeftAnchor(lstViewer, 0.0);
        AnchorPane.setRightAnchor(lstViewer, 0.0);
        AnchorPane.setBottomAnchor(lstViewer, 0.0);
        contentPane.getChildren().add(lstViewer);

        initializeLogView();
        initializeToolbar();
        initializeRegisters();
    }

    /**
     * Configures the logger and it's related components. This includes changing the
     * log level through the menu bar as well as the log view itself.
     */

    private void initializeLogView() {

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
    }

    /**
     * Initializes the single <i>bits</i> (Components) of the STATUS register.
     */

    private void initializeStatusRegister() {

        // Setup STATUS register table view

        irpBit.setCellValueFactory(new PropertyValueFactory<>("irpFlag"));
        rp1Bit.setCellValueFactory(new PropertyValueFactory<>("rp1Flag"));
        rp0Bit.setCellValueFactory(new PropertyValueFactory<>("rp0Flag"));
        toBit.setCellValueFactory(new PropertyValueFactory<>("toFlag"));
        pdBit.setCellValueFactory(new PropertyValueFactory<>("pdFlag"));
        zBit.setCellValueFactory(new PropertyValueFactory<>("zeroFlag"));
        dcBit.setCellValueFactory(new PropertyValueFactory<>("digitCarryFlag"));
        cBit.setCellValueFactory(new PropertyValueFactory<>("carryFlag"));
    }

    /**
     * Initialize register related components. This includes SFRs as well as GPRs.
     */

    private void initializeRegisters() {

        // Setup Special Function Register table view

        sfrName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Use custom factory for printing as hex string in prefix format
        sfrValue.setCellValueFactory(param -> new SimpleStringProperty(String.format("0x%02X", (byte) param.getValue().getValue())));

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
        gprValue.setCellValueFactory(param -> new SimpleStringProperty(String.format("0x%02X", (byte) param.getValue().getValue())));

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

        // Initialize working register
        workingRegister.setText(String.format("0x%02X", simulator.getExecutor().getWorkingRegister()));
        // Initialize instruction register
        instructionRegister.setText(String.format("0x%04X", simulator.getExecutor().getInstructionRegister()));

        initializeStatusRegister();
    }

    /**
     * Configures the single tools inside of the toolbar.
     */

    private void initializeToolbar() {

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
    }

    /**
     * Initializes the simulator with all related properties and bindings.
     */

    private void initializeSimulator() {

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

        // Register memory change listeners

        simulator.getRam().addPropertyChangeListener(new RamMemoryChangeListener());
        simulator.getStack().addPropertyChangeListener(new StackMemoryChangeListener());
        simulator.getExecutor().addPropertyChangeListener(new ExecutorChangeListener());
    }

    /**
     * Initializes the service layer and all related properties as well as
     * the result listeners.
     */

    private void initializeServices() {

        // Service for loading a lst file

        lstReaderService = new LstReaderService();

        lstReaderService.setOnSucceeded((event) -> {

            lstViewer.replaceText(event.getSource().getValue().toString());
            lstViewer.moveTo(0, 0);

            try {

                // Load lst file to simulator
                simulator.load(lstReaderService.getFile());

            } catch (Exception exc) {

                ApplicationDialog.showError(exc);
                exc.printStackTrace(System.err);
            }
        });

        // Service for initiating next execution step

        nextStepService = new NextStepService();

        nextStepService.setOnSucceeded((event) -> {

            lstViewer.setIndicator(lstViewer.addressToLineNumber((Integer) event.getSource().getValue()));
        });

        // Service for stopping execution flow

        stopExecutionService = new StopExecutionService();

        stopExecutionService.setOnSucceeded(event -> {

            lstViewer.setIndicator(lstViewer.addressToLineNumber(0x00));
        });

        // Service for continue execution until breakpoint is reached

        runExecutionService = new RunExecutionService();

        runExecutionService.valueProperty().addListener((observable, prevAddress, address) -> {

            lstViewer.setIndicator(lstViewer.addressToLineNumber(null != address ? address : prevAddress));
        });

        // Bind executing property to services

        executingProperty = new SimpleBooleanProperty();

        executingProperty.bind(Bindings.or(
                runExecutionService.runningProperty(),
                nextStepService.runningProperty()));
    }

    @FXML
    private void onQuitAction(ActionEvent event) {

        Optional<ButtonType> option = ApplicationDialog.showQuitConfirm();

        if (option.isPresent() && ButtonType.OK == option.get()) {

            simulator.stop();
            Platform.exit();
        }
    }

    @FXML
    private void onOpenAction(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open LST File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("LST Files", "*.LST"));

        File file = fileChooser.showOpenDialog(null);

        if (null != file) {

            if (lstReaderService.isRunning() || executingProperty.getValue()) {

                ApplicationDialog.showWarning("The program is still running. Please stop it " +
                        "before loading a new one.");

            } else {

                lstReaderService.reset();
                lstReaderService.setFile(file);
                lstReaderService.start();
            }
        }
    }

    @FXML
    private void onBreakpointAction(ActionEvent event) {

        lstViewer.toggleBreakpoint();
    }

    @FXML
    private void onNextStepAction(ActionEvent event) {

        if (nextStepService.isRunning()) {

            ApplicationDialog.showWarning("The program is already running. This operation " +
                    "is invalid until it has stopped.");

        } else {

            nextStepService.reset();
            nextStepService.setSimulator(simulator);
            nextStepService.start();
        }
    }

    @FXML
    private void onStopAction(ActionEvent event) {

        if (stopExecutionService.isRunning()) {

            ApplicationDialog.showWarning("The program isn't running or is about to " +
                    "be terminated. This operation is invalid until it is running again.");

        } else {

            stopExecutionService.reset();
            stopExecutionService.setSimulator(simulator);
            stopExecutionService.start();
        }
    }

    @FXML
    private void onRunAction(ActionEvent event) {

        if (runExecutionService.isRunning()) {

            ApplicationDialog.showWarning("The program is already running. This operation " +
                    "is invalid until it has stopped.");

        } else {

            runExecutionService.reset();
            runExecutionService.setSimulator(simulator);
            runExecutionService.setLstViewer(lstViewer);
            runExecutionService.start();
        }
    }

    @FXML
    private void onAboutAction(ActionEvent event) {

        ApplicationDialog.showAbout();
    }

    @FXML
    private void onObserveRegisterAction(ActionEvent event) {

        int address = addressField.getValue();
        byte value = null == simulator.getRam().get(address) ? 0 : simulator.getRam().get(address);

        // Check if entry already exists

        FilteredList<GeneralPurposeRegister> filtered = generalRegisters.getItems()
                .filtered(register -> address == register.getAddress());

        if (filtered.isEmpty()) {

            // Add new observer if it doesn't exist

            GeneralPurposeRegister register = new GeneralPurposeRegister();

            register.setAddress(address);
            register.setValue(value);

            generalRegisters.getItems().add(register);
            generalRegisters.refresh();
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

                    byte value = (byte) event.getNewValue(); // Value of STATUS register
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

                    Platform.runLater(() -> {

                        statusRegister.getItems().setAll(status);
                        statusRegister.refresh();
                    });

                } else if (0x0C > ((IndexedPropertyChangeEvent) event).getIndex()) {

                    int address = ((IndexedPropertyChangeEvent) event).getIndex();
                    byte value = (byte) event.getNewValue(); // Value of the SFR
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

                        specialRegisters.refresh();
                    });

                } else if (0x0C <= ((IndexedPropertyChangeEvent) event).getIndex()) {

                    // Update just observed General Purpose Registers

                    int address = ((IndexedPropertyChangeEvent) event).getIndex();

                    FilteredList<GeneralPurposeRegister> filtered = generalRegisters.getItems()
                            .filtered(register -> address == register.getAddress());

                    if (!filtered.isEmpty()) {

                        // Only one match should exist, just uses the first one

                        filtered.get(0).setValue((byte) event.getNewValue());
                        generalRegisters.refresh();
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

    private class ExecutorChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {

            if (event.getPropertyName().equals("workingRegister")) {

                workingRegister.setText(String.format("0x%02X", (byte) event.getNewValue()));

            } else if (event.getPropertyName().equals("instructionRegister")) {

                instructionRegister.setText(String.format("0x%04X", (short) event.getNewValue()));
            }
        }
    }
}
