package main;

import game.mills.HumanPlayer;
import game.mills.NewGame;
import gui.MillGameUI;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize players
        HumanPlayer humanPlayer1 = new HumanPlayer("HumanPlayer 1", Color.BLACK);
        HumanPlayer humanPlayer2 = new HumanPlayer("HumanPlayer 2", Color.WHITE);

        // Initialize the game
        NewGame game = new NewGame(humanPlayer1, humanPlayer2);

        // Create the MillGameUI instance
        MillGameUI ui = new MillGameUI(primaryStage, game);

        // Pass the UI reference to the game
        game.setUI(ui);
    }

    public static void main(String[] args) {
        launch(args);
    }
}