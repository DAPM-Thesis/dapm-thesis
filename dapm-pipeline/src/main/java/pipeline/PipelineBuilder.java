package pipeline;

import communication.Consumer;
import communication.Producer;
import communication.Publisher;
import communication.Subscriber;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;

import java.util.UUID;

public class PipelineBuilder {
    private Pipeline currentPipeline;

    public PipelineBuilder createPipeline() {
        currentPipeline = new Pipeline();
        return this;
    }

    public PipelineBuilder addProcessingElement(ProcessingElement pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        currentPipeline.getProcessingElements().add(pe);
        return this;
    }

    public <O extends Message> PipelineBuilder connect(Publisher<O> from, Subscriber<Message> to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        // fetch from's output channel if it exists, and create a new one otherwise
        Producer producer = currentPipeline.getReceivingChannels().get(from);
        if (producer == null) {
            String topic = "Topic" + UUID.randomUUID();
            producer = new Producer(topic);
            from.registerProducer(producer);

            Consumer consumer = new Consumer(to, topic);
            to.registerConsumer(consumer);

            currentPipeline.getReceivingChannels().put((ProcessingElement) from, producer);
            currentPipeline.getChannels().add(producer);
        }
        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }
}
