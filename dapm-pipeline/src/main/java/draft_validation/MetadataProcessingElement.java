package draft_validation;

import communication.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetadataProcessingElement {
    private final String orgID;
    private final String templateID;
    private final List<Class<? extends Message>> inputs;
    private final Class<? extends Message> output;
    private final int instanceID;

    public MetadataProcessingElement(String orgID,
                                     String templateID,
                                     List<Class<? extends Message>> inputs,
                                     Class<? extends Message> output,
                                     int instanceID) {
        assert orgID != null && templateID != null && inputs != null: "stop being lazy.";
        assert !orgID.isEmpty() && !templateID.isEmpty() : "indistinguishable orgID and templateID";
        assert instanceID > 0 : "instanceID must be positive integer by convention (CFG definition).";

        this.orgID = orgID;
        this.templateID = templateID;
        this.inputs = inputs;
        this.output = output;
        this.instanceID = instanceID;

        assert !(isSource() && isSink()) : "Processing elements must either have inputs or output or both.";
    }

    /** returns a copy of the MetaProcessingElement instance's inputs List */
    public List<Class<? extends Message>> getInputs() { return new ArrayList<>(inputs); }

    /** returns a copy of the MetaProcessingElement's outputs List */
    public Class<? extends Message> getOutput() { return output; }

    public Class<? extends Message> typeAt(int index) { return inputs.get(index); }

    public int inputCount() { return inputs.size(); }

    public boolean isSource() {
        return inputs.isEmpty();
    }

    public boolean isSink() {
        return output == null;
    }

    @Override
    public String toString() {
        return "MPE[" + orgID + "," + templateID + "," + inputs.size() + "," + output + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof MetadataProcessingElement otherMPE)) { return false; }
        return orgID.equals(otherMPE.orgID)
                && templateID.equals(otherMPE.templateID)
                && inputs.equals(otherMPE.inputs)
                && (output == null ? otherMPE.output == null : output.equals(otherMPE.output))
                && instanceID == otherMPE.instanceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgID, templateID, inputs, output);
    }
}
