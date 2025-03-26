package pipeline;

import communication.channel.Channel;
import communication.channel.ChannelFactory;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.Source;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pipeline {
    private Set<ProcessingElement> processingElements;
    private Set<Channel<?>> channels;
    private Map<ProcessingElement, Channel<?>> receivingChannels;

    public Pipeline(ChannelFactory channelFactory) {
        processingElements = new HashSet<>();
        channels = new HashSet<>();
        receivingChannels = new HashMap<>();
    }

    public Set<ProcessingElement> getProcessingElements() { return processingElements; }
    public Map<ProcessingElement, Channel<?>> getReceivingChannels() { return receivingChannels; }
    public Set<Channel<?>> getChannels() { return channels; }

    public void start() {
        for(ProcessingElement pe : processingElements) {
            if(pe instanceof Source<?> source) {
                source.start();
            }
        }
    }
}
