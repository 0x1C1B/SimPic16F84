package org.ai2ra.hso.simpic16f84.ui.component;

import javafx.beans.property.SetProperty;
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

        IntFunction<Node> numberFactory = LineNumberFactory.get(this);
        IntFunction<Node> breakpointFactory = new BreakpointFactory(this);

        IntFunction<Node> graphicFactory = line -> {

            HBox hbox = new HBox(numberFactory.apply(line),
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

    /**
     * Toggles a breakpoint for the currently selected line inside of
     * the LST view. With selection, the current caret position (line)
     * is meant. If breakpoint is already set, it is removed otherwise
     * it is set.
     */

    public void toggleBreakpoint() {

        int line = this.offsetToPosition(
                this.getCaretPosition(), TwoDimensional.Bias.Forward).getMajor();

        if(breakpoints.contains(line)) {

            breakpoints.remove(line);

        } else {

            breakpoints.add(line);
        }
    }
}
