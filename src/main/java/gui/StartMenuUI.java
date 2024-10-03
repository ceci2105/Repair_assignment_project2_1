package gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class StartMenuUI {
    private Stage primaryStage;

    public StartMenuUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupStartMenu();
    }

    private void setupStartMenu() {
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Nine Men's Morris Game");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: 'Arial';");

        Button startButton = new Button("Start New Game");
        startButton.setOnAction(e -> startGame());

        Button rulesButton = new Button("How to play");
        rulesButton.setOnAction(e -> new RulesUI().display());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        menuBox.getChildren().addAll(titleLabel, startButton, rulesButton, exitButton);

        Scene startMenuScene = new Scene(menuBox, 600, 600);
        primaryStage.setScene(startMenuScene);
        primaryStage.setTitle("Nine Men's Morris - Start Menu");
        primaryStage.show();
    }

    private void startGame() {
        new MillGameUI(primaryStage); // This will switch to the game UI
    }
}