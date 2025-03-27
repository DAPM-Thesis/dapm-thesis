package pipeline.processingelement.operator;

import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.algorithm.Algorithm;
import communication.Publisher;
import communication.Subscriber;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Subscriber<Message>, Publisher<O> {
    private final Algorithm<Message, AO> algorithm;
    private Subscriber<O> outgoing; // channel

    public Operator(Algorithm<Message, AO> algorithm) { this.algorithm = algorithm; }

    @Override
    public void observe(Message input) {
        AO algorithmOutput = algorithm.run(input);
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

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
