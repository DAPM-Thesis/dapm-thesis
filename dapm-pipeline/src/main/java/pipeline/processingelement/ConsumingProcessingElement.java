package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;
import communication.config.ConsumerConfig;
import communication.message.Message;

import java.util.HashMap;
import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement implements Subscriber<Message> {
    /** Holds the input types and their multiplicities. So if processing element consumes Event's from two
     *  separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     *  (PetriNet.class, 1).*/
    protected final Map<Class<? extends Message>, Integer> inputs;
    private final Map<Integer, Consumer> consumers = new HashMap<>();

    public ConsumingProcessingElement(Configuration configuration) {
        super(configuration);
        this.inputs = setConsumedInputs();
        for (int typeCount : inputs.values()) {
            assert typeCount > 0 : "every provided input type must occur a positive number of times.";
        }
    }

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs(); // TODO: refactor with annotations

    public void registerConsumer(ConsumerConfig config) {
        Consumer consumer = new Consumer(this, config);
        consumer.start();
        consumers.put(config.portNumber(), consumer);
    }
}
