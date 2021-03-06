package org.ai2ra.hso.simpic16f84.ui.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import org.ai2ra.hso.simpic16f84.ui.util.BreakpointFactory;
import org.ai2ra.hso.simpic16f84.ui.util.LineIndicatorFactory;
import org.ai2ra.hso.simpic16f84.ui.util.SyntaxHighlighting;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.TwoDimensional;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.io.IOException;
import java.time.Duration;
import java.util.function.IntFunction;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Read-only LST file viewer component with syntax highlighting and support
 * for breakpoints.
 *
 * @see CodeArea
 * @author 0x1C1B
 */

public class LstViewer extends CodeArea {

    private SetProperty<Integer> breakpoints;
    private IntegerProperty indicator;

    public LstViewer() {

        // Load view

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/LstViewer.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {

            loader.load();

        } catch(IOException exc) {

            throw new RuntimeException(exc);
        }

        this.setEditable(false); // Just a viewer not an editor

        // Enable breakpoints and line indicators

        breakpoints = new SimpleSetProperty<>(FXCollections.observableSet());
        indicator = new SimpleIntegerProperty();

        IntFunction<Node> numberFactory = LineNumberFactory.get(this);
        IntFunction<Node> indicatorFactory = new LineIndicatorFactory(this);
        IntFunction<Node> breakpointFactory = new BreakpointFactory(this);

        IntFunction<Node> graphicFactory = line -> {

            HBox hbox = new HBox(numberFactory.apply(line),
                    indicatorFactory.apply(line),
                    breakpointFactory.apply(line));

            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };

        this.setParagraphGraphicFactory(graphicFactory);

        // Enable key listeners for setting breakpoints

        InputMap<Event> inputMap = InputMap.consume(
                keyPressed(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN)),
                (ignore) -> toggleBreakpoint());

        Nodes.addInputMap(this, inputMap);

        // Enable syntax highlighting, updated all 500 ms after typing is stopped/ text is set

        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(change -> this.setStyleSpans(0, SyntaxHighlighting.compute(this.getText())));

        // Resets breakpoints and line indicator if new LST file is loaded

        this.textProperty().addListener((observable, oldValue, newValue) -> {

            // Clear all breakpoints for the new program
            breakpoints.clear();

            // Sets the line indicator to first valid execution line
            indicator.set(addressToLineNumber(0x00));
        });
    }

    public ObservableSet<Integer> getBreakpoints() {

        return breakpoints.get();
    }

    public SetProperty<Integer> breakpointsProperty() {

        return breakpoints;
    }

    public void setBreakpoints(ObservableSet<Integer> breakpoints) {

        this.breakpoints.set(breakpoints);
    }

    public int getIndicator() {

        return indicator.get();
    }

    public IntegerProperty indicatorProperty() {

        return indicator;
    }

    /**
     * Sets the indicator line to the given one. If the given line is invalid because it
     * doesn't contain machine instructions, the method rejects.
     *
     * @param lineNumber The line number should be set as execution line
     * @return Returns true if line was set, otherwise false
     */

    public boolean setIndicator(int lineNumber) {

        // Allow setting execution line only for lines containing machine instructions

        if (hasMachineInstructions(lineNumber)) {

            this.indicator.set(lineNumber);
            return true;
        }

        return false;
    }

    /**
     * Converts a given address to a line number by trying to find the address as
     * part of the machine instructions.
     *
     * @throws IllegalStateException Thrown if no line with address was found
     * @param address The machine instruction address
     * @return Returns the number of line which contains the given address
     */

    public int addressToLineNumber(int address) {

        for (int lineNumber = 1; lineNumber < this.getParagraphs().size(); ++lineNumber) {

            if (hasMachineInstructions(lineNumber)) {

                if (Integer.parseInt(this.getText(lineNumber).substring(0, 4), 16) == address) {

                    return lineNumber;
                }
            }
        }

        throw new IllegalStateException("Address doesn't exist");
    }

    /**
     * Toggles a breakpoint for the currently selected line inside of
     * the LST view. With selection, the current caret position (line)
     * is meant. If breakpoint is already set, it is removed otherwise
     * it is set.
     */

    public void toggleBreakpoint() {

        int lineNumber = this.offsetToPosition(
                this.getCaretPosition(), TwoDimensional.Bias.Forward).getMajor();

        // Allow breakpoints only for lines containing machine instructions

        if (hasMachineInstructions(lineNumber)) {

            if (breakpoints.contains(lineNumber)) {

                breakpoints.remove(lineNumber);

            } else {

                breakpoints.add(lineNumber);
            }
        }
    }

    /**
     * Checks whether a line contains machine instructions or not. That's the
     * prerequisite for setting breakpoints or the indicator to it.
     *
     * @param lineNumber The line number to check
     * @return Returns true if line contains machine instruction, otherwise false
     */

    private boolean hasMachineInstructions(int lineNumber) {

        return this.getText(lineNumber).matches("^[0-9a-fA-F]{4} [0-9a-fA-F]{4}.*$");
    }
}
