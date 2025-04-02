package pipeline.processingelement.operator;

import communication.Producer;
import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import algorithm.Algorithm;
import communication.Consumer;
import communication.Publisher;
import communication.Subscriber;

import java.util.Collection;
import java.util.HashSet;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Subscriber<Message>, Publisher<O> {
    private final Algorithm<Message, AO> algorithm;
    private final Collection<Consumer> consumers = new HashSet<>();
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
    public void registerProducer(String connectionTopic, String brokerURL) {
        this.producer = new Producer(connectionTopic, brokerURL);
    }

    @Override
    public void registerConsumer(String connectionTopic, String brokerURL) {
        consumers.add(new Consumer(this, connectionTopic, brokerURL));
    }
}
