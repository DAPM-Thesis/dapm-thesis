package pipeline;

import communication.channel.Channel;
import communication.channel.ChannelFactory;
import communication.Publisher;
import communication.Subscriber;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.Sink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pipeline {
    private Set<ProcessingElement> processingElements;
    private Set<Channel<?>> channels;
    private Map<ProcessingElement, Channel<?>> receivingChannels;
    private ChannelFactory channelFactory;

    public Pipeline(ChannelFactory channelFactory) {
        processingElements = new HashSet<>();
        channels = new HashSet<>();
        receivingChannels = new HashMap<>();
        this.channelFactory = channelFactory;
    }

    public Pipeline(Set<ProcessingElement> processingElements,
                    Set<Channel<?>> channels,
                    Map<ProcessingElement, Channel<?>> receivingChannels,
                    ChannelFactory channelFactory) {

        this(channelFactory);

        if (!areConsistent(processingElements, channels, receivingChannels)) {
            throw new IllegalArgumentException("The given arguments are inconsistent");
        }

        this.processingElements = processingElements;
        this.channels = channels;
        this.receivingChannels = receivingChannels;
    }

    private boolean areConsistent(Set<ProcessingElement> processingElements,
                                  Set<Channel<?>> channels,
                                  Map<ProcessingElement, Channel<?>> receivingChannels) {
        // Any processing element must either have an output channel or be a sink (but not both).
        for (ProcessingElement pe : processingElements) {
            if (pe instanceof Sink && receivingChannels.containsKey(pe)
                    || !(pe instanceof Sink) && !receivingChannels.containsKey(pe)) {
                return false;
            }
        }
        // the pipeline must only receive data from within the pipeline
        return channels.equals(receivingChannels.values());
    }

    public Pipeline addProcessingElement(ProcessingElement pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        processingElements.add(pe);
        return this;
    }

    public <C> Pipeline connect(Publisher<C> from, Subscriber<C> to) {
        if (!processingElements.contains(from) || !processingElements.contains(to))
            { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        // fetch from's output channel if it exists, and create a new one otherwise
        Channel<C> channel = (Channel<C>) receivingChannels.get(from);
        if (channel == null) {
            channel = channelFactory.createChannel();
            from.subscribe(channel);
            receivingChannels.put((ProcessingElement) from, channel);
            channels.add(channel);
        }

        channel.subscribe(to);
        return this;
    }

}
