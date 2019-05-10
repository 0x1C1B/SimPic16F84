package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.ai2ra.hso.simpic16f84.ui.component.LstViewer;

import java.util.function.IntFunction;

/**
 * Responsible for rendering a line indicator, that indicates the current
 * execution line.
 *
 * @author 0x1C1B
 * @see LstViewer
 */

public class LineIndicatorFactory implements IntFunction<Node> {

    private final LstViewer viewer;

    public LineIndicatorFactory(LstViewer viewer) {

        this.viewer = viewer;
    }

    @Override
    public Node apply(int line) {

        Polygon indicator = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
        indicator.setFill(Color.GREEN);

        // Allow dynamic removing/adding of the breakpoint

        BooleanBinding isVisible = Bindings.createBooleanBinding(() ->
                        viewer.executionLineProperty().getValue().equals(line),
                viewer.executionLineProperty());

        indicator.visibleProperty().bind(isVisible);

        return indicator;
    }
}
