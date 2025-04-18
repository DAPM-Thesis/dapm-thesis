package pipeline;

import draft_validation.ProcessingElementReference;

import java.util.*;

public class Pipeline {
    private String organizationOwnerID;
    private Set<ProcessingElementReference> processingElements;
    private Map<String, ProcessingElementReference> sources;
    private Map<ProcessingElementReference, ProcessingElementReference> connections;

    public Pipeline(String organizationOwnerID) {
        processingElements = new HashSet<>();
        connections = new HashMap<>();
        sources = new HashMap<>();
        this.organizationOwnerID = organizationOwnerID;
    }

    public Map<String, ProcessingElementReference> getSources() {
        return sources;
    }

    public String getOrganizationOwnerID() {
        return organizationOwnerID;
    }

    public Set<ProcessingElementReference> getProcessingElements() {
        return processingElements;
    }

    public Map<ProcessingElementReference, ProcessingElementReference> getConnections() {
        return connections;
    }
}
