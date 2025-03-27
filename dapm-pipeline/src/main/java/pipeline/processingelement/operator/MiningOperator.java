package pipeline.processingelement.operator;

import communication.message.Message;
import algorithm.MiningAlgorithm;
import utils.Pair;

public abstract class MiningOperator<O extends Message> extends Operator<Pair<O, Boolean>, O> {
    public MiningOperator(MiningAlgorithm<Message, O> miningAlgorithm) {
        super(miningAlgorithm);
    }

    @Override
    protected O convertAlgorithmOutput(Pair<O, Boolean> rawOutput) {
        // Extract the O value from the Pair.
        // Optionally, you could also log or use the Boolean flag.
        return rawOutput.getFirst();
    }

}
