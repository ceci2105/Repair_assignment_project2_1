package main;

import game.mills.Game;
import game.mills.HumanPlayer;
import gui.MillGameUI;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;


/**
 * Main class that starts our Mills game.
 * This class extends the JavaFX Application class and launches the game GUI.
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application by initializing the players, the game, and the GUI.
     * 
     * @param primaryStage the main stage for this application, onto which the game UI is set
     */
    @Override
    public void start(Stage primaryStage) {
        // Initialize players with name and stone color
        HumanPlayer humanPlayer1 = new HumanPlayer("HumanPlayer 1", Color.BLACK);
        HumanPlayer humanPlayer2 = new HumanPlayer("HumanPlayer 2", Color.WHITE);

        // Initialize the game with the two players
        Game game = new Game(humanPlayer1, humanPlayer2);
        
        MillGameUI ui = new MillGameUI(primaryStage, game);

        // Pass the UI reference to the game
        game.setUI(ui);
    }

    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}