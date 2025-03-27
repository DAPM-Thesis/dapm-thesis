package pipeline.processingelement.operator;

import algorithm.Algorithm;
import communication.message.Message;

public abstract class SimpleOperator<O extends Message> extends Operator<O, O> {
    public SimpleOperator(Algorithm<Message, O> algorithm) {
        super(algorithm);
    }

    @Override
    protected O convertAlgorithmOutput(O algorithmOutput) { return algorithmOutput; }

    @Override
    protected boolean publishCondition(O algorithmOutput) { return true; };

}

