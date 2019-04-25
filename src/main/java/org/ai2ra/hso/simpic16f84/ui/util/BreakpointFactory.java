package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.function.IntFunction;

/**
 * This class is responsible for rendering the breakpoints inside of a
 * {@link org.fxmisc.richtext.CodeArea CodeArea}.
 *
 * @author 0x1C1B
 */

public class BreakpointFactory implements IntFunction<Node> {

    private ObservableSet<Integer> breakpoints;

    public BreakpointFactory(ObservableSet<Integer> breakpoints) {

        this.breakpoints = breakpoints;
    }

    @Override public Node apply(int line) {

        Circle breakpoint = new Circle(4);
        breakpoint.setFill(Color.RED);
        breakpoint.setVisible(false);

        // Allow dynamic removing/adding of the breakpoint

        breakpoints.addListener((SetChangeListener<Integer>) change -> {

            int changed = change.wasAdded() ? change.getElementAdded() :
                    change.getElementRemoved();

            if(changed == line) {

                breakpoint.setVisible(change.wasAdded());
            }
        });

        return breakpoint;
    }
}
