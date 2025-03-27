package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;

import java.util.Collection;
import java.util.HashSet;

public abstract class Sink<I> extends ProcessingElement implements Subscriber<I> {

    private final Collection<Consumer<?>> consumers = new HashSet<>();

    @Override
    public abstract void observe(I input);

    @Override
    public void registerConsumer(Subscriber<I> subscriber) {
        consumers.add((Consumer<I>)subscriber);
    }
}
