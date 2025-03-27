package pipeline;

import communication.Producer;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.Source;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pipeline {
    private Set<ProcessingElement> processingElements;
    private Set<Producer> channels;
    private Map<ProcessingElement, Producer> receivingChannels;

    public Pipeline() {
        processingElements = new HashSet<>();
        channels = new HashSet<>();
        receivingChannels = new HashMap<>();
    }

    public Set<ProcessingElement> getProcessingElements() { return processingElements; }
    public Map<ProcessingElement, Producer> getReceivingChannels() { return receivingChannels; }
    public Set<Producer> getChannels() { return channels; }

    public void start() {
        for(ProcessingElement pe : processingElements) {
            if(pe instanceof Source<?> source) {
                source.start();
            }
        }
    }
}
