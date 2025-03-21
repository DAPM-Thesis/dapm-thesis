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

    public Pipeline addProcessingElement(ProcessingElement pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        processingElements.add(pe);
        return this;
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
