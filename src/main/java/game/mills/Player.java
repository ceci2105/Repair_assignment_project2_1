package game.mills;

import javafx.scene.paint.Color;

public interface Player {
    String getName();
    Color getColor();
    int getStonesToPlace();
    int getStonesOnBoard();
    void decrementStonesToPlace();
    void incrementStonesOnBoard();
    void decrementStonesOnBoard();
}
