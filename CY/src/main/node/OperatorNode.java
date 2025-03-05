package main.node;

import main.Message;
import main.datatype.DataType;
import main.Topic;
import main.node.handle.InputHandle;
import main.node.handle.OutputHandle;
import main.observerpattern.Subscriber;

import java.util.Collection;
import java.util.HashSet;

public abstract class OperatorNode<T extends DataType> extends Node implements Subscriber<Message<T>> {
    protected final Collection<InputHandle<?>> inputHandles;
    protected final OutputHandle<T> outputHandle;

    public OperatorNode(String name, String description, Collection<Topic<?>> inputTopics) {
        super(name, description);

        // create input handles
        inputHandles = new HashSet<>();
        for (Topic<?> topic : inputTopics) {
            addInputHandle(topic);
        }

        // create output handle
        Topic<T> outputTopic = new Topic<>();
        outputHandle = new OutputHandle<>(outputTopic);
    }

    /** Creates a (new) input handle for the node.
     * @param topic the topic that the input handle subscribes to receive streamed items.*/
    private <U extends DataType> void addInputHandle(Topic<U> topic) {
        InputHandle<U> handle = InputHandle.createForTopic(topic);
        inputHandles.add(handle);
    }

    public Topic<?> getOutputTopic() { return outputHandle.getTopic(); }

    public void publish(Message<T> message) {
        this.outputHandle.publish(message);
    }

}
