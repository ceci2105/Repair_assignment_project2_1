package agents.neural_network;

import lombok.Getter;
import lombok.Setter;
import org.tensorflow.types.TFloat16;

@Setter
@Getter
public class BoardState {
    private TFloat16 beforeState;
    private TFloat16 afterState;

    public BoardState(TFloat16 beforeState, TFloat16 afterState) {
        this.afterState = afterState;
        this.beforeState = beforeState;
    }
}
