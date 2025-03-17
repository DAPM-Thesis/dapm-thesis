package node.handle;

import datatype.DataType;
import datatype.serialization.DataTypeSerializer;
import model.Message;
import service.Producer;

/** The component of Node which is responsible for publishing node output to topics. */
public class OutputHandle<T extends DataType> extends Handle<T> {

    private final Producer producer;

    public OutputHandle() {
        producer = new Producer();
    }

    public void publish(Message<T> msg) {
        DataTypeSerializer serializer = new DataTypeSerializer();
        msg.data().acceptVisitor(serializer);
        String serialization = serializer.getSerialization();
        this.producer.publish(this.topic.getName(), new Message<>(serialization));
    }

}
