package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RulesUI {

    public void display() {
        Stage window = new Stage();
        window.setTitle("How to Play Nine Men's Morris");

        // Label to display game rules
        Label rulesLabel = new Label(getGameRules());
        rulesLabel.setWrapText(true);

        // Scroll pane for scrolling through the rules
        ScrollPane scrollPane = new ScrollPane(rulesLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        // Button to close the window
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

        // Layout using VBox
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 20, 20, 20));
        layout.getChildren().addAll(scrollPane, closeButton);

        // Set up the scene
        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
        window.showAndWait(); // Use showAndWait to focus on this window
    }

    private String getGameRules() {
        return "Nine Men's Morris Rules:\n\n"
            + "1. The board consists of a grid with twenty-four intersections or points. Each player has nine pieces and the goal is to form a mill: three stones aligned horizontally or vertically, allowing a player to remove an opponent's stone from the game board.\n\n"
            + "2. Black starts first and players then take turns placing their stones onto empty points on the board. When all stones have been placed, players take turns moving a stone to an adjacent point.\n\n"
            + "3. The game is won by the player who reduces their opponent to two pieces, or by blocking all possible moves of their opponent.\n\n"
            + "4. If a player forms a mill, they may remove one of their opponent's stones from the board. This stone cannot be removed from a mill. If all opponent's stones are in mills, any stone can be removed.\n\n"
            + "5. If a player is reduced to three pieces, they may jump to any empty point on the board.\n\n"
            + "Enjoy the game!";
    }
}