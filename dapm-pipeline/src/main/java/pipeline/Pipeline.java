package pipeline;

import communication.channel.Channel;
import communication.channel.ChannelFactory;
import communication.Publisher;
import communication.Subscriber;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;

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

    public Set<ProcessingElement> getProcessingElements() {
        return processingElements;
    }

    public Map<ProcessingElement, Channel<?>> getReceivingChannels() {
        return receivingChannels;
    }

    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public Set<Channel<?>> getChannels() {
        return channels;
    }

    public void start() {
        for(ProcessingElement pe : processingElements) {
            if(pe instanceof Source<?> source) {
                source.start();
            }
        }
    }
}
