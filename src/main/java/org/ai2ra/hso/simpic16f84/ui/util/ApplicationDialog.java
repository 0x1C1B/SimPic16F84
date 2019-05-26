package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for displaying application related dialogs.
 *
 * @author 0x1C1B
 */

public class ApplicationDialog {

    /**
     * Displays an error dialog which shows the exception's stack trace as well
     * as a human-readable error message/description.
     *
     * @param exc The thrown exception
     */

    public static void showError(Throwable exc) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(exc.getMessage());

        Label label = new Label("Stack Trace:");
        TextArea detailsArea = new TextArea();
        VBox contentPane = new VBox();

        // Write stack trace to text area

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        detailsArea.setText(sw.toString());

        contentPane.getChildren().addAll(label, detailsArea);

        alert.getDialogPane().setContent(contentPane);
        alert.showAndWait();
    }

    /**
     * Shows a warning dialog to inform the user about a possible problem.
     *
     * @param text The warning text
     */

    public static void showWarning(String text) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(text);
        alert.showAndWait();
    }
}
