package pipeline;

import pipeline.accesscontrolled.processingelement.AccessControlledProcessingElement;
import pipeline.processingelement.*;

import java.util.*;

public class Pipeline {
    /*private String organizationOwnerID;
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

     */

    private String organizationOwnerID;
    private Set<AccessControlledProcessingElement<?>> acpeSet; // All ACPEs in the pipeline
    private Map<AccessControlledProcessingElement<?>, AccessControlledProcessingElement<?>> connections;

    public Pipeline(String organizationOwnerID) {
        this.organizationOwnerID = organizationOwnerID;
        this.acpeSet = new HashSet<>();
        this.connections = new HashMap<>();
    }

    public String getOrganizationOwnerID() {
        return organizationOwnerID;
    }

    public Set<AccessControlledProcessingElement<?>> getACPEs() {
        return acpeSet;
    }

    /**
     * Returns the source ACPEs (those with no upstream).
     */
    public Set<AccessControlledProcessingElement<?>> getSources() {
        Set<AccessControlledProcessingElement<?>> sources = new HashSet<>();
        for (AccessControlledProcessingElement<?> acpe : acpeSet) {
            if (!connections.containsValue(acpe)) {
                sources.add(acpe);
            }
        }
        return sources;
    }

    /**
     * Adds an ACPE to this pipeline.
     */
    public void addACPE(AccessControlledProcessingElement<?> acpe) {
        if (acpe != null) {
            acpeSet.add(acpe);
        }
    }
    public void addConnection(AccessControlledProcessingElement<?> from, AccessControlledProcessingElement<?> to) {
        connections.put(from, to);
    }
}
