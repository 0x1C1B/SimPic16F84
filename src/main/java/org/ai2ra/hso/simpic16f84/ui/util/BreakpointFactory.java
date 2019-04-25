package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;

import java.util.function.IntFunction;

/**
 * This class is responsible for rendering the breakpoints inside of a
 * {@link LstViewer LstViewer}.
 *
 * @see LstViewer
 * @author 0x1C1B
 */

public class BreakpointFactory implements IntFunction<Node> {

    private final LstViewer viewer;

    public BreakpointFactory(LstViewer viewer) {

        this.viewer = viewer;
    }

    @Override public Node apply(int line) {

        Circle breakpoint = new Circle(4);
        breakpoint.setFill(Color.RED);

        // Allow dynamic removing/adding of the breakpoint

        BooleanBinding isVisible = Bindings.createBooleanBinding(() ->
                viewer.breakpointsProperty().contains(line), viewer.breakpointsProperty());

        breakpoint.visibleProperty().bind(isVisible);

        return breakpoint;
    }
}
