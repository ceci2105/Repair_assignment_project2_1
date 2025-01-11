//Represents the AI agent using Alpha-Beta pruning

package AlphaBetaPruning;

import game.mills.Board;
import game.mills.Game;
import game.mills.Node;
import game.mills.Player;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * The AlphaBetaAIPlayer class represents an AI-controlled player using Alpha-Beta pruning.
 */
@Log
public class AlphaBetaAIPlayer implements Player {
    private final int depth;
    private final AlphaBetaAlgorithm alphaBeta;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Color color;
    @Getter
    private int stonesToPlace;
    @Getter
    private int stonesOnBoard;
    @Setter
    private Game game;

    public AlphaBetaAIPlayer(String name, Color color, Game game, int depth) {
        this.name = name;
        this.color = color;
        this.depth = depth;
        this.game = game;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
        this.alphaBeta = new AlphaBetaAlgorithm(game, depth);
    }

    public void makeMove(Board board, int phase) {
        Platform.runLater(() -> {
            if (phase == 1) {
                int bestPlacement = alphaBeta.findBestPlacement(board, this);
                game.placePiece(bestPlacement);
            } else {
                Node[] bestMove = alphaBeta.findBestMove(board, this, phase);
                if (bestMove[0] != null && bestMove[1] != null) {
                    game.makeMove(bestMove[0].getId(), bestMove[1].getId());
                }
            }
        });
    }

    public void decrementStonesToPlace() {
        stonesToPlace--;
        stonesOnBoard++;
    }

    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }
}