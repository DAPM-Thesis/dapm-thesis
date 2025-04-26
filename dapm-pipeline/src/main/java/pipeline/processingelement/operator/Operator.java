package pipeline.processingelement.operator;

import communication.Producer;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ConsumingProcessingElement;
import communication.Publisher;
import utils.LogUtil;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
                                                      implements Publisher<O> {
    private Producer producer;

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
    public void terminate() {
        if (producer != null) {
            producer.terminate();
            producer = null;
        }
    }

    @Override
    public void registerProducer(ProducerConfig config) {
        if (this.producer == null) {
            this.producer = new Producer(config);
        }
        else {
            LogUtil.debug("Producer already registered for operator.");
        }
    }
}
