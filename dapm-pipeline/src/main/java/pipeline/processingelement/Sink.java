package pipeline.processingelement;

import communication.Subscriber;
import communication.message.Message;

public abstract class Sink extends ConsumingProcessingElement implements Subscriber<Message> {

}
