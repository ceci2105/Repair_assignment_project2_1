package gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The {@code StartMenuUI} class is responsible for creating and displaying the
 * start menu of the Nine Men's Morris game. It provides buttons to start a new
 * game,
 * view the game rules, and exit the application.
 */
public class StartMenuUI {
    private Stage primaryStage;
    private static final String humanGame = "humanGame";
    private static final String baselineGame = "baselineGame";
    private static final String minimaxGame = "minimaxGame";
    private static final String selfPlay = "SelfPlay";
    private static final String run100Games = "run100Games";
    private static final String COLLECT_DATA = "collectData";
    private static final String mctsGame = "mctsGame";

    /**
     * Constructor for the start menu user interface.
     * 
     * @param primaryStage The primary stage on which to set the scene.
     */
    public StartMenuUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupStartMenu();
    }

    /**
     * Sets up the start menu layout including title, buttons, and their
     * corresponding actions.
     */
    private void setupStartMenu() {
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Nine Men's Morris Game");
        titleLabel.setStyle(
                "-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: 'Arial';");

        Button startButton = new Button("Start New Game");
        startButton.setOnAction(e -> startGame());

        Button startbaseagentButton = new Button("Start New Game vs Baseline Agent");
        startbaseagentButton.setOnAction(e -> startbaselineGame());

        Button startminimaxButton = new Button("Start New Game vs Minimax Agent");
        startminimaxButton.setOnAction(e -> startminimaxGame());

        Button startbaselineminimaxButton = new Button("Start Minimax Agent vs Minimax Agent");
        startbaselineminimaxButton.setOnAction(e -> startSelfPlayGame());

        Button startmctsplayerButton = new Button("Start New Game vs MCTS Player");
        startmctsplayerButton.setOnAction(e -> startmctsGame());

        Button run100gamesButton = new Button("Run 100 games");
        run100gamesButton.setOnAction(e -> run100Games());

        Button collectDataButton = new Button("Collect Training Data");
        collectDataButton.setOnAction(e -> startDataCollection());

        Button rulesButton = new Button("How to play");
        rulesButton.setOnAction(e -> new RulesUI().display());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        menuBox.getChildren().addAll(titleLabel, startButton, startbaseagentButton, startminimaxButton,
                startbaselineminimaxButton, startmctsplayerButton, run100gamesButton, rulesButton, collectDataButton, exitButton);

        // Creating the scene and setting it on the stage
        Scene startMenuScene = new Scene(menuBox, 700, 700);
        primaryStage.setScene(startMenuScene);
        primaryStage.setTitle("Nine Men's Morris - Start Menu");
        primaryStage.show();
    }

    /**
     * Initializes the game UI to start the corresponding game mode.
     */
    private void startGame() {
        new MillGameUI(primaryStage, humanGame); 
    }

    private void startbaselineGame() {
        new MillGameUI(primaryStage, baselineGame);
    }

    private void startminimaxGame() {
        new MillGameUI(primaryStage, minimaxGame); 
    }

    private void startSelfPlayGame() {
        new MillGameUI(primaryStage, selfPlay); 
    }

    private void startmctsGame() {
        new MillGameUI(primaryStage, mctsGame);
    }

    private void run100Games() {
        new MillGameUI(primaryStage, run100Games);
    }

    private void startDataCollection() {
        new MillGameUI(primaryStage, COLLECT_DATA);
    }

}