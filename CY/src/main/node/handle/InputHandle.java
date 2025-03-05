package main.node.handle;

import main.Message;
import main.datatype.DataType;
import main.Topic;
import main.observerpattern.Publisher;
import main.observerpattern.Subscriber;

/** Input handle is responsible for receiving messages from a topic and publishing them to its node */
public class InputHandle<T extends DataType> extends Handle<T> implements Subscriber<Message<T>>, Publisher<Message<T>> {
    private Subscriber<Message<T>> node;

    // private constructor to indicate that inputhandles should only be created by Nodes (via createForTopic)
    private InputHandle(Topic<T> topic) {
        super(topic);
    }

    @Override
    public void observe(Message<T> message) {
        // Pass on the message received from the Topic to the input handle's Node.
        publish(message);
    }

    public static <T extends DataType> InputHandle<T> createForTopic(Topic<T> topic) {
        InputHandle<T> handle = new InputHandle<>(topic);
        topic.subscribe(handle);
        return handle;
    }

    @Override
    public void subscribe(Subscriber<Message<T>> subscriber) {
        this.node = subscriber;
    }

    @Override
    public void unsubscribe(Subscriber<Message<T>> subscriber) {
        throw new IllegalCallerException("An input handle's lifetime is the same as the node it is created for. It should never unsubscribe from its node.")
    }

    @Override
    public void publish(Message<T> message) {
        assert node != null : "An input handle must always be attached to a node.";
        node.observe(message);
    }
}
