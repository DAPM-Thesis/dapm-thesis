package pipeline.processingelement;

public record ProcessingElementReference(String organizationID, String organizationHostURL, String templateID, int instanceNumber,
                                         ProcessingElementType processingElementType) {

}