package pipeline;

import candidate_validation.ProcessingElementReference;
import utils.graph.DG;

import java.util.*;
import java.util.stream.Collectors;

public class Pipeline {
    private final String owningOrganizationID;
    private final Map<String, ProcessingElementReference> processingElements;
    private final DG<ProcessingElementReference, Integer> directedGraph;

    public Pipeline(String owningOrganizationID, DG<ProcessingElementReference, Integer> directedGraph) {
        processingElements = new HashMap<>();
        this.directedGraph = directedGraph;
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

    public DG<ProcessingElementReference, Integer> getDirectedGraph() {
        return directedGraph;
    }

    public String getInstanceID(ProcessingElementReference ref) {
        return processingElements.entrySet().stream()
                .filter(entry -> entry.getValue().equals(ref))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<ProcessingElementReference> getSinks() {
        return directedGraph.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSink)
                .collect(Collectors.toSet());
    }

    public Set<ProcessingElementReference> getSources() {
        return directedGraph.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSource)
                .collect(Collectors.toSet());
    }
}
