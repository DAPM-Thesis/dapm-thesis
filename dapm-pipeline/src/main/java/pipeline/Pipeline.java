package pipeline;

import draft_validation.ProcessingElementReference;

import java.util.*;

public class Pipeline {
    private final String organizationOwnerID;
    private Map<String, ProcessingElementReference> processingElements;

    public Pipeline(String organizationOwnerID) {
        processingElements = new HashMap<>();
        this.organizationOwnerID = organizationOwnerID;
    }

    public String getOrganizationOwnerID() {
        return organizationOwnerID;
    }

    public Map<String, ProcessingElementReference> getProcessingElements() {return processingElements;}

    public void addProcessingElement(String instanceID, ProcessingElementReference processingElementReference) {
        processingElements.put(instanceID, processingElementReference);
    }
}
