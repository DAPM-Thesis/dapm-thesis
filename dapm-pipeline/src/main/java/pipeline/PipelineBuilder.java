package pipeline;

import communication.Publisher;
import communication.Subscriber;
import communication.channel.Channel;
import communication.channel.ChannelFactory;
import pipeline.processingelement.ProcessingElement;

public class PipelineBuilder {
    private ChannelFactory channelFactory;
    private Pipeline currentPipeline;

    public PipelineBuilder createPipeline(ChannelFactory channelFactory) {
        currentPipeline = new Pipeline(channelFactory);
        this.channelFactory = channelFactory;
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
        Channel<C> channel = (Channel<C>) currentPipeline.getReceivingChannels().get(from);
        if (channel == null) {
            channel = channelFactory.createChannel();
            from.subscribe(channel);
            currentPipeline.getReceivingChannels().put((ProcessingElement) from, channel);
            currentPipeline.getChannels().add(channel);
        }

        channel.subscribe(to);
        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }
}
