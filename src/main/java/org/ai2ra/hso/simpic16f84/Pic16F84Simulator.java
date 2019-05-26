package org.ai2ra.hso.simpic16f84;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Application entry class of the graphical simulator. This class is responsible for
 * loading the GUI (Graphical User Interface) as well as the simulator itself.
 *
 * @author 0x1C1B
 * @author Freddy1096
 * @see org.ai2ra.hso.simpic16f84.ui.controller.SimulatorController
 */

public class Pic16F84Simulator extends Application {

    @Override public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/view/SimulatorView.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png"), 16, 16, true, true));
        primaryStage.setTitle("SimPic16F84");
        primaryStage.setMinWidth(320);
        primaryStage.setMinHeight(240);
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
