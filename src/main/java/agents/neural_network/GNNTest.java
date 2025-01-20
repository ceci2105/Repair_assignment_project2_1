package agents.neural_network;

import game.mills.Board;
import game.mills.Game;
import game.mills.HumanPlayer;
import game.mills.Player;
import javafx.scene.paint.Color;
import lombok.extern.java.Log;
import org.deeplearning4j.nn.graph.vertex.impl.InputVertex;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


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

        List<INDArray> data = DataLoader.readData();
        GNN gnn = new GNN();
        gnn.fit(new INDArray[]{data.get(0), data.get(1)});

    }
}
