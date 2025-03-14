package node.handle;

import datatype.DataType;
import model.Topic;
// TODO: remove type from handle??? Seems like it makes sense for it to have it, but currently unused.
public abstract class Handle<T extends DataType> {
    Topic topic;

    public Handle() { }

    public void setTopic(Topic topic) {this.topic = topic;}

    public Topic getTopic() { return topic; }
}
