package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

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

    public static void showAbout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("SimPic16F84 v0.0.1");

        VBox contentPane = new VBox();
        Text copyright = new Text("Copyright © 2018 Freddy1096, 0x1C1B");
        Hyperlink webpage = new Hyperlink("MIT License");
        TextFlow license = new TextFlow(
                new Text("Licensed under the terms of the"), webpage);

        webpage.setOnAction((event) -> {

            try {

                Desktop.getDesktop().browse(new URL("https://opensource.org/licenses/MIT").toURI());

            } catch (URISyntaxException | IOException exc) {

                exc.printStackTrace();
            }
        });

        contentPane.getChildren().addAll(copyright, license);

        alert.getDialogPane().setContent(contentPane);
        alert.showAndWait();
    }
}
