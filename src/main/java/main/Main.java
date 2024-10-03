package main;

import gui.StartMenuUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class that starts the Mills game.
 * This class extends the JavaFX Application class and launches the game GUI.
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application by initializing the game UI.
     *
     * @param primaryStage the main stage for this application, onto which the game UI is set
     */
    @Override
    public void start(Stage primaryStage) {
        // Create an instance of the MillGameUI, which will initialize and display the game
        new StartMenuUI(primaryStage);
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args); // Launches the JavaFX application
    }
}