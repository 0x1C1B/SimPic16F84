package org.ai2ra.hso.simpic16f84.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Service for reading a LST file. The result is used for displaying the content
 * inside of a text component.
 *
 * @author 0x1C1B
 */

public class LstReaderService extends Service<String> {

    private File file;

    public File getFile() {

        return file;
    }

    public void setFile(File file) {

        this.file = file;
    }

    @Override
    protected Task<String> createTask() {

        return new Task<String>() {

            @Override
            protected String call() throws Exception {

                StringBuilder builder = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                    String line;

                    while (null != (line = reader.readLine())) {

                        builder.append(line).append(System.lineSeparator());
                    }
                }

                return builder.toString();
            }
        };
    }
}
