package pipeline.processingelement;

import communication.message.Message;

import java.util.Map;

public abstract class Sink extends ConsumingProcessingElement {

    public Sink(Configuration configuration) { super(configuration); }

    @Override
    public abstract void observe(Message input, int portNumber);
}
