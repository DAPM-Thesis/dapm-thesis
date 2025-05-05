package pipeline.processingelement;

import communication.message.Message;
import security.token.PEToken;

public abstract class Sink extends ConsumingProcessingElement {
    protected Sink(PEToken initialToken) {
        super(initialToken);
    }
    private volatile boolean heartbeatStarted = false;

    @Override 
    protected abstract void handle(Message message, int port);
}
