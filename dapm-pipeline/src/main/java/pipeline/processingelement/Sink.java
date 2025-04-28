package pipeline.processingelement;

import communication.message.Message;

public abstract class Sink extends ConsumingProcessingElement {

    @Override
    public abstract void observe(Message input, int portNumber);
}
