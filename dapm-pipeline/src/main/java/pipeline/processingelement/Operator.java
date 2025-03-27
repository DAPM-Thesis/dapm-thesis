package pipeline.processingelement;

import algorithm.Algorithm;
import communication.Consumer;
import communication.Publisher;
import communication.Subscriber;

import java.util.Collection;
import java.util.HashSet;

public abstract class Operator<I,O,AI,AO> extends ProcessingElement implements Subscriber<I>, Publisher<O> {
    private final Algorithm<AI,AO> algorithm;
    private final Collection<Consumer<?>> consumers = new HashSet<>();
    private Publisher<O> producer;

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
    public void publish(O output) { producer.publish(output); }

    @Override
    public void registerProducer(Publisher<O> producer) {
        this.producer = producer;
    }

    @Override
    public void registerConsumer(Subscriber<I> subscriber) {
        consumers.add((Consumer<I>)subscriber);
    }

    @Override
    public boolean unsubscribe(Subscriber<O> subscriber) {
        return true;
    }
}
