package main.node;

import main.Message;
import main.datatype.DataType;
import main.Topic;
import main.node.handle.InputHandle;
import main.node.handle.OutputHandle;
import main.observerpattern.Subscriber;

import java.util.Collection;

public abstract class OperatorNode<T extends DataType> extends Node implements Subscriber<Message<T>> {

    protected final OutputHandle<T> outputHandle;

    public OperatorNode(String name, String description) {
        super(name, description);

        // create output handle
        Topic outputTopic = new Topic(name + "_output");
        outputHandle = new OutputHandle<>(outputTopic);
    }

    /**
     * Creates a (new) input handle for the node.
     *
     * @param topic the topic that the input handle subscribes to receive streamed items.
     * @return A (new) InputHandle subscribed to the given topic.
     */
    protected  <U extends DataType> InputHandle<U> makeInputHandle(Topic topic) {
        return InputHandle.createForTopic(topic, this);
    }

    public Topic getOutputTopic() { return outputHandle.getTopic(); }

    public void publish(Message<T> message) {
        this.outputHandle.publish(message);
    }

}
