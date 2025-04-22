package pipeline.processingelement.operator;

import communication.message.Message;

public abstract class SimpleOperator<O extends Message> extends Operator<O, O> {

    @Override
    protected O convertAlgorithmOutput(O algorithmOutput) { return algorithmOutput; }

    @Override
    protected boolean publishCondition(O algorithmOutput) { return true; }

}

