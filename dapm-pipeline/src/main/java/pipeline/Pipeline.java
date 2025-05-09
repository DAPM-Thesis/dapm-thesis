package pipeline;

import candidate_validation.ProcessingElementReference;

import java.util.*;

public class Pipeline {
    private final String owningOrganizationID;
    private final Map<String, ProcessingElementReference> processingElements;

    public Pipeline(String owningOrganizationID) {
        processingElements = new HashMap<>();
        this.owningOrganizationID = owningOrganizationID;
    }

    public String getOwningOrganizationID() {
        return owningOrganizationID;
    }

    // return an unmodifiable map to ensure that processing elements are only added via addProcessingElement()
    public Map<String, ProcessingElementReference> getProcessingElements() {return Collections.unmodifiableMap(processingElements);}

    public void addProcessingElement(String instanceID, ProcessingElementReference processingElementReference) {
        processingElements.put(instanceID, processingElementReference);
    }
}
