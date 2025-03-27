package pipeline.processingelement;

import communication.Publisher;
import communication.Subscriber;
import communication.message.Message;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Subscriber<O> outgoing; // Channel

    public void start() {
        while(isAvailable()) {
            O output = process();
            publish(output);
        }
    }

    public abstract O process();

    @Override
    public void publish(O data) { outgoing.observe(data); }

    @Override
    public boolean subscribe(Subscriber<O> subscriber) {
        if (outgoing != null && subscriber != outgoing) // only succeed when the value has not already been set
            { return false; }
        outgoing = subscriber;
        return true;
    }

    @Override
    public boolean unsubscribe(Subscriber<O> subscriber) {
        if (outgoing != subscriber || subscriber == null) { return false; }
        outgoing = null;
        return true;
    }

}
