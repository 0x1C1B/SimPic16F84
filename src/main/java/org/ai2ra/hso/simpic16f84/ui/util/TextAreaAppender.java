package org.ai2ra.hso.simpic16f84.ui.util;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom log4j appender for writing logs to <code>TextArea</code> component
 * of the JavaFX framework.
 *
 * @author 0x1C1B
 */

public class TextAreaAppender extends WriterAppender {

    private static volatile TextArea textArea;

    public static void setTextArea(final TextArea textArea) {

        TextAreaAppender.textArea = textArea;
    }

    @Override
    public void append(LoggingEvent event) {

        final String MESSAGE = this.layout.format(event);

        try {

            Platform.runLater(() -> {

                try {

                    textArea.selectEnd();
                    textArea.insertText(textArea.getText().length(), MESSAGE);

                } catch (Exception exc) {

                    exc.printStackTrace(System.err);
                }
            });

        } catch (IllegalStateException exc) {

            // Thrown if JavaFX platform isn't already initialized, ignore this case

            exc.printStackTrace(System.err);
        }
    }
}
