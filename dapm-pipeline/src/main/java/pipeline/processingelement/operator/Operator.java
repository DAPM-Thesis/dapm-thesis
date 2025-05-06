package pipeline.processingelement.operator;

import communication.Producer;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ConsumingProcessingElement;
import communication.Publisher;

import java.util.Map;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Publisher<O> {
    private Producer producer;

    public Operator(Configuration configuration) { super(configuration); }

    @Override
    public void observe(Message input, int portNumber) {
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
