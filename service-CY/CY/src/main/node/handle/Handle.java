package main.node.handle;

import main.datatype.DataType;
import main.Topic;

public abstract class Handle<T extends DataType> {
    Topic<T> topic;

    public Handle(Topic<T> topic) { this.topic = topic; }

    public Topic<T> getTopic() { return topic; }
}
