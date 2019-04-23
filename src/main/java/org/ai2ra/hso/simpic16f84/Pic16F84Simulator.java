package org.ai2ra.hso.simpic16f84;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Pic16F84Simulator extends Application {

    @Override public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/view/SimulatorView.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Pic16F84Simulator");
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
