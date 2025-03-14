package node.handle;

import model.Message;
import datatype.DataType;
import model.Topic;
import observerpattern.Publisher;
import observerpattern.Subscriber;
import service.Consumer;
import datatype.serialization.deserialization.DataTypeFactory;
import datatype.DataType;
import observerpattern.Publisher;
import observerpattern.Subscriber;
import service.Consumer;

import java.util.Collection;

/** Input handle is responsible for receiving messages from a topic and publishing them to its node */
public class InputHandle<T extends DataType> extends Handle<T> implements Subscriber<Message<String>>, Publisher<Message<T>> {
    private Subscriber<Message<T>> node;
    private Consumer consumer;

    public InputHandle() {
        consumer = new Consumer();
    }

    @Override
    public void observe(Message<String> message) {
        // Pass on the message received from the Topic to the input handle's Node.
        try {
            T data = (T) DataTypeFactory.deserialize(message.data());
            Message<T> dataTypeMessage = new Message<>(data);
            System.out.println("Received in input handle: " + dataTypeMessage);
            publish(dataTypeMessage);
        } catch (Exception e) {
            System.out.println("Error in observe: " + message.data());
            throw new RuntimeException(e);
        }

    }

    public void setTopic(Topic topic) {
        this.topic = topic;
        consumer.subscribe(topic.getName(), this);
    }

    @Override
    public void subscribe(Subscriber<Message<T>> subscriber) {
        this.node = subscriber;
    }

    @Override
    public void unsubscribe(Subscriber<Message<T>> subscriber) {
        throw new IllegalCallerException("An input handle's lifetime is the same as the node it is created for. It should never unsubscribe from its node.");
    }

    @Override
    public void publish(Message<T> message) {
        assert node != null : "An input handle must always be attached to a node.";
        node.observe(message);
    }
}
