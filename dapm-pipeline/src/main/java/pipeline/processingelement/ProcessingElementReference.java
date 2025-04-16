package pipeline.processingelement;

public record ProcessingElementReference(String organizationID, int processElementID,
                                         ProcessingElementType processingElementType) {

}