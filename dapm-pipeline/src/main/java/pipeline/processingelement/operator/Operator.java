package pipeline.processingelement.operator;

import communication.Producer;
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
    public void publish(O data) {
        producer.publish(data);
    }

    @Override
    public boolean pause() {
        return super.pause() && producer.pause();
    }

    @Override
    public boolean terminate() {
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

    @Override
    public void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
