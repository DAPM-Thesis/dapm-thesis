package pipeline;

import communication.Consumer;
import communication.Producer;
import communication.Publisher;
import communication.Subscriber;
import communication.channel.ChannelFactory;
import pipeline.processingelement.ProcessingElement;

public class PipelineBuilder {
    private ChannelFactory channelFactory;
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

    public <C> PipelineBuilder connect(Publisher<C> from, Subscriber<C> to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        // fetch from's output channel if it exists, and create a new one otherwise
        Producer<C> producer = (Producer<C>) currentPipeline.getReceivingChannels().get(from);
        if (producer == null) {
            String topic = String.valueOf(currentPipeline.getChannels().size());
            producer = new Producer<>(topic);
            producer.registerProducer(from);
            from.registerProducer(producer);

            Consumer<C> consumer = new Consumer<C>(topic);
            consumer.registerConsumer(to);
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
