package pipeline.processingelement.operator;

import communication.message.Message;
import pipeline.processingelement.accesscontrolled.PEToken;

public abstract class SimpleOperator<O extends Message> extends Operator<O, O> {

    protected SimpleOperator(PEToken initialToken) {
        super(initialToken);
    }

    @Override
    protected O convertAlgorithmOutput(O algorithmOutput) { return algorithmOutput; }

    @Override
    protected boolean publishCondition(O algorithmOutput) { return true; }

}

