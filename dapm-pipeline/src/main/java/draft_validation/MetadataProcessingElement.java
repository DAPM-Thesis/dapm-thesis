package draft_validation;

import communication.message.Message;

import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "MPE[" + orgID + "," + templateID + "," + inputs.size() + "," + outputs.size() + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof MetadataProcessingElement otherMPE)) { return false; }
        return orgID.equals(otherMPE.orgID)
                && templateID.equals(otherMPE.templateID)
                && inputs.equals(otherMPE.inputs)
                && outputs.equals(otherMPE.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgID, templateID, inputs, outputs);
    }
}
