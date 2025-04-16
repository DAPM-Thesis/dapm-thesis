package pipeline.processingelement;

import communication.Consumer;
import communication.message.Message;

import java.util.Collection;
import java.util.HashSet;

public abstract class Sink extends ConsumingProcessingElement {

    private final Collection<Consumer> consumers = new HashSet<>();

    @Override
    public abstract void observe(Message input);
}
