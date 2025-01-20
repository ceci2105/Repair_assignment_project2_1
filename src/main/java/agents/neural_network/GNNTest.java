package agents.neural_network;

import game.mills.Board;
import game.mills.Game;
import game.mills.HumanPlayer;
import game.mills.Player;
import javafx.scene.paint.Color;
import lombok.extern.java.Log;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


@Log
public class GNNTest {
    public static void main(String[] args) {
        Board board  = new Board();
        Player player1 = new HumanPlayer("P1", Color.WHITE);
        Player player2 = new HumanPlayer("P2", Color.BLACK);
        Game game = new Game(player1, player2);

        board.placePiece(player1, 0);
        board.placePiece(player2, 1);
        board.placePiece(player1, 2);

        INDArray boardArray = GNN.boardToINDArray(board, game);
        INDArray expectedArray = Nd4j.zeros(24);
        expectedArray.putScalar(0,1);
        expectedArray.putScalar(1,2);
        expectedArray.putScalar(2,1);

        boolean equal = boardArray.equals(expectedArray);
        System.out.println(boardArray);
        System.out.println(expectedArray);
        GNN gnn = new GNN();

    }

}
