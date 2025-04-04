package draft_validation;

import communication.message.Message;

import java.util.List;

public class MetadataProcessingElement {
    private final String orgID;
    private final String templateID;
    private final List<Class<? extends Message>> inputs;
    private final List<Class<? extends Message>> outputs;

    public MetadataProcessingElement(String orgID,
                                     String templateID,
                                     List<Class<? extends Message>> inputs,
                                     List<Class<? extends Message>> outputs) {
        this.orgID = orgID;
        this.templateID = templateID;
        this.inputs = inputs;
        this.outputs = outputs;
    }
}
