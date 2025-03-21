package pipeline.processingelement;

import algorithm.Algorithm;
import communication.Publisher;
import communication.Subscriber;

public abstract class Operator<I,O,AI,AO> extends ProcessingElement implements Subscriber<I>, Publisher<O> {
    private final Algorithm<AI,AO> algorithm;
    private Subscriber<O> outgoing; // channel

    public Operator(Algorithm<AI,AO> algorithm) { this.algorithm = algorithm; }

    @Override
    public void observe(I input) {
        AI algorithmInput = convertInput(input);
        AO algorithmOutput = algorithm.run(algorithmInput);
        if (publishCondition(algorithmOutput)) {
            O operatorOutput = convertOutput(algorithmOutput);
            publish(operatorOutput);   
        }
    }

    protected abstract boolean publishCondition(AO algorithmOutput);

    protected abstract AI convertInput(I i);

    protected abstract O convertOutput(AO o);

    @Override
    public void publish(O output) { outgoing.observe(output); }

    @Override
    public boolean subscribe(Subscriber<O> subscriber) {
        if (outgoing != null && subscriber != outgoing) { return false; }
        outgoing = subscriber;
        return true;
    }

    @Override
    public boolean unsubscribe(Subscriber<O> subscriber) {
        if (subscriber != outgoing || outgoing == null) { return false; }
        this.outgoing = null;
        return true;
    }
}
