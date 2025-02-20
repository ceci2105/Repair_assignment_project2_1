package agents.neural_network;

import game.mills.Game;
import game.mills.InvalidMove;
import game.mills.Node;
import game.mills.Player;
import gui.MillGameUI;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The baseline agent, that always makes a completely pseudorandom move.
 */

public class BaselineAgent implements Player {
    @Getter
    private String name;
    @Getter @Setter
    private Color color;
    @Getter
    private int stonesToPlace;
    @Getter
    private int stonesOnBoard;
    @Setter
    private Game game;

    /**
     * Constructor for the BaselineAgent class.
     * @param name  The name of the player.
     * @param color The color of the player's pieces.
     */
    public BaselineAgent(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
    }


    @Override
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    @Override
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    @Override
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }

    public void makeMove() {
        if (stonesToPlace > 0) {
            placePiece();
        } else {
            movePiece();
        }
    }

    /**
     * Places a piece on the board.
     */
    private void placePiece() {
        List<Integer> nodeIndices = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            nodeIndices.add(i);
        }
        Collections.shuffle(nodeIndices); // Shuffle the node indices to randomize the placement

        for (int i : nodeIndices) {
            Node node = game.getBoard().getNode(i);
            if (!node.isOccupied()) {
                try {
                    game.placePiece(i);
                    MillGameUI.incrementBaselineMoves();
                    if (game.isMillFormed()) {
                        removeOpponentPiece();
                    }
                    break;
                } catch (InvalidMove e) {
                    // Move to the next node if the move is invalid
                    continue;
                }
            }
        }
    }

    /**
     * Moves a piece on the board.
     */
    private void movePiece() {
        boolean canFly = game.canFly(this);
        List<Integer> nodeIndices = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            nodeIndices.add(i);
        }
        Collections.shuffle(nodeIndices); // Shuffle to add randomness
    
        for (int fromIndex : nodeIndices) {
            Node fromNode = game.getBoard().getNode(fromIndex);
            if (fromNode.isOccupied() && fromNode.getOccupant() == this) {
                List<Integer> possibleToIndices = new ArrayList<>();
    
                if (canFly) {
                    // Can fly to any empty node
                    for (int toIndex = 0; toIndex < 24; toIndex++) {
                        Node toNode = game.getBoard().getNode(toIndex);
                        if (!toNode.isOccupied()) {
                            possibleToIndices.add(toIndex);
                        }
                    }
                } else {
                    // Only adjacent nodes
                    for (Node toNode : game.getBoard().getNeighbours(fromNode)) {
                        if (!toNode.isOccupied()) {
                            possibleToIndices.add(toNode.getId());
                        }
                    }
                }
    
                Collections.shuffle(possibleToIndices); // Shuffle possible moves
                for (int toIndex : possibleToIndices) {
                    try {
                        game.makeMove(fromNode.getId(), toIndex);
                        MillGameUI.incrementBaselineMoves();
                        if (game.isMillFormed()) {
                            removeOpponentPiece();
                        }
                        return;
                    } catch (InvalidMove e) {
                        // If the move is invalid, try the next one
                    }
                }
            }
        }
        // If no valid moves are found
    }

    /**
     * Removes a random opponents piece
     */
    private void removeOpponentPiece() {
        for (int i = 0; i < 24; i++) {
            Node node = game.getBoard().getNode(i);
            if (node.isOccupied() && node.getOccupant().getColor() == Color.BLACK && !game.getBoard().isPartOfMill(node)) {
                try {
                    game.removePiece(i);
                    break;
                } catch (InvalidMove e) {
                    // Move to the next node if the removal is invalid
                    continue;
                }
            } else if (node.isOccupied() && node.getOccupant().getColor() == Color.WHITE && !game.getBoard().isPartOfMill(node)) {
                try {
                    game.removePiece(i);
                    break;
                } catch (InvalidMove e) {
                    continue;
                }
            }
        }
    }


}
