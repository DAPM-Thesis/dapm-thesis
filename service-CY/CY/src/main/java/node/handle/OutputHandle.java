package node.handle;

import datatype.DataType;
import model.Topic;
import datatype.serialization.DataTypeSerializer;
import datatype.serialization.DataTypeVisitor;
import model.Message;
import service.Producer;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    private final Producer<String> producer;

    public OutputHandle() {
        producer = new Producer<>();
    }

    public void publish(Message<T> msg) {
        DataTypeSerializer serializer = new DataTypeSerializer();
        msg.data().acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        this.producer.publish(this.topic.getName(), new Message<>(serialization));
    }

}
