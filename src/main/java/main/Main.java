// Main.java
package main;

import game.mills.NewGame;
import gui.MillGameUI;
import game.mills.Game;
import game.mills.Player;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize players
        Player player1 = new Player("Player 1", Color.BLACK);
        Player player2 = new Player("Player 2", Color.WHITE);

        // Initialize the game
        NewGame game = new NewGame(player1, player2);

        // Start the GUI
        new MillGameUI(primaryStage, game);
    }

    public static void main(String[] args) {
        launch(args);
    }
}