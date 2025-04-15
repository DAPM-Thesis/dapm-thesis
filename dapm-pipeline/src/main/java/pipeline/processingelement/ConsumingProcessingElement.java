package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;
import communication.message.Message;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement implements Subscriber<Message> {
    /** Holds the input types and their multiplicities. So if processing element consumes Event's from two
     *  separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     *  (PetriNet.class, 1).*/
    protected final Map<Class<? extends Message>, Integer> inputs;
    private final Collection<Consumer> consumers = new HashSet<>();

    protected ConsumingProcessingElement() {
        this.inputs = setConsumedInputs();
        for (int typeCount : inputs.values()) {
            assert typeCount > 0 : "every provided input type must occur a positive number of times.";
        }
    }

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs();


    public void registerConsumer(String connectionTopic, String brokerURL) {
        Consumer consumer = new Consumer(this, connectionTopic, brokerURL);
        consumer.start(); // could we add the isavailable check here? based on the token
        consumers.add(consumer);
    }
}
