package pipeline.processingelement.operator;

import communication.message.Message;
import security.token.PEToken;
import utils.Pair;

public abstract class MiningOperator<O extends Message> extends Operator<Pair<O, Boolean>, O> {

    protected MiningOperator(PEToken initialToken) {
        super(initialToken);
    }

    @Override
    protected O convertAlgorithmOutput(Pair<O, Boolean> rawOutput) {
        // Extract the O value from the Pair.
        // Optionally, you could also log or use the Boolean flag.
        return rawOutput.first();
    }

}
