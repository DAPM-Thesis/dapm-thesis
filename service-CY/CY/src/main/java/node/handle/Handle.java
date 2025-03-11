package node.handle;

import datatype.DataType;
import datatype.DataType;
import model.Topic;

public abstract class Handle<T extends DataType> {
    Topic topic;

    public Handle() { }

    public void setTopic(Topic topic) {this.topic = topic;}

    public Topic getTopic() { return topic; }
}
