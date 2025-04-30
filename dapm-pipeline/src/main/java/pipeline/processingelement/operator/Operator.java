package pipeline.processingelement.operator;

import communication.Producer;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import pipeline.processingelement.accesscontrolled.PEToken;
import communication.Publisher;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Publisher<O> {
    protected Operator(PEToken initialToken) {
        super(initialToken);
    }

    private Producer producer;

    @Override
    public void handle(Message input, int portNumber) {
        AO algorithmOutput = process(input, portNumber);
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract AO process(Message input, int portNumber);

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

    @Override
    public void publish(O output) { producer.publish(output); }

    @Override
    public void registerProducer(ProducerConfig config) {
        this.producer = new Producer(config);
    }
}
