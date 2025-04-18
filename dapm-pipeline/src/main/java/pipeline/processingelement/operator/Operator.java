package pipeline.processingelement.operator;

import communication.Producer;
import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import algorithm.Algorithm;
import communication.Publisher;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Publisher<O> {
    private final Algorithm<Message, AO> algorithm;
    private Producer producer;

    public Operator(Algorithm<Message, AO> algorithm) { this.algorithm = algorithm; }

    @Override
    public void observe(Message input) {
        AO algorithmOutput = algorithm.run(input);
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

    @Override
    public void publish(O output) { producer.publish(output); }

    @Override
    public void registerProducer(String brokerURL, String topic) {
        this.producer = new Producer(brokerURL, topic);
    }
}
