package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;
import communication.message.Message;

import java.util.Collection;
import java.util.HashSet;

public abstract class Sink extends ConsumingProcessingElement implements Subscriber<Message> {

    private final Collection<Consumer> consumers = new HashSet<>();

    @Override
    public abstract void observe(Message input);

    @Override
    public void registerConsumer(Consumer consumer) {
        consumers.add(consumer);
    }
}
