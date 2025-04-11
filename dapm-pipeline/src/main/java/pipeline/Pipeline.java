package pipeline;

import pipeline.processingelement.*;

import java.util.*;

public class Pipeline {
    private String organizationOwnerID;
    private Set<ProcessingElementReference> processingElements;
    private Set<ProcessingElementReference> sources;
    private Map<ProcessingElementReference, ProcessingElementReference> connections;

    public Pipeline(String organizationOwnerID) {
        processingElements = new HashSet<>();
        connections = new HashMap<>();
        sources = new HashSet<>();
        this.organizationOwnerID = organizationOwnerID;
    }

    public Set<ProcessingElementReference> getSources() {return sources;}
    public String getOrganizationOwnerID() {return organizationOwnerID;}
    public Set<ProcessingElementReference> getProcessingElements() { return processingElements; }
    public Map<ProcessingElementReference, ProcessingElementReference> getConnections() { return connections; }
}
