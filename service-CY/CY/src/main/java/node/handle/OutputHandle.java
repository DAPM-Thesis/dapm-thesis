package node.handle;

import datatype.DataType;
import datatype.serialization.DataTypeSerializer;
import datatype.serialization.DataTypeVisitor;
import main.Message;
import main.Topic;
import service.Producer;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    private final Producer<String> producer;

    public OutputHandle(Topic topic) {
        super(topic);
        producer = new Producer<>();
    }

    public void publish(Message<T> msg) {
        DataTypeSerializer serializer = new DataTypeSerializer();
        msg.data().acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        this.producer.publish(getTopic().getName(), new Message<>(serialization));
    }

}
