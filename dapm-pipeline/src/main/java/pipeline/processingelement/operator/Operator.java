package pipeline.processingelement.operator;

import communication.Producer;
import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import communication.Publisher;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Publisher<O> {
    private Producer producer;

    @Override
    public void observe(Message input) {
        AO algorithmOutput = process(input);
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract AO process(Message input);

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

    @Override
    public void publish(O output) { producer.publish(output); }

    @Override
    public void registerProducer(String brokerURL, String topic) {
        this.producer = new Producer(brokerURL, topic);
    }
}
