package pipeline.processingelement;

import communication.Subscriber;

public abstract class Sink<I> extends ProcessingElement implements Subscriber<I> {

}
