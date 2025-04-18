package draft_validation;

import communication.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessingElementReference {
    private final String organizationID;
    private final String organizationHostURL;
    private final String templateID;
    private final List<Class<? extends Message>> inputs;
    private final Class<? extends Message> output;
    private final int instanceNumber;

    public ProcessingElementReference(String organizationID,
                                      String organizationHostURL,
                                      String templateID,
                                      List<Class<? extends Message>> inputs,
                                      Class<? extends Message> output,
                                      int instanceNumber) {
        assert organizationID != null && templateID != null && inputs != null: "stop being lazy.";
        assert !organizationID.isEmpty() && !templateID.isEmpty() : "indistinguishable orgID and templateID";
        assert instanceNumber > 0 : "instanceID must be positive integer by convention (CFG definition).";

        this.organizationID = organizationID;
        this.organizationHostURL = organizationHostURL;
        this.templateID = templateID;
        this.inputs = inputs;
        this.output = output;
        this.instanceNumber = instanceNumber;

        assert !(isSource() && isSink()) : "Processing elements must either have inputs or output or both.";
    }

    public String getOrganizationHostURL() { return this.organizationHostURL; }

    /** returns a copy of the inputs */
    public List<Class<? extends Message>> getInputs() { return new ArrayList<>(inputs); }

    public Class<? extends Message> getOutput() { return output; }

    public Class<? extends Message> typeAt(int index) { return inputs.get(index); }

    public int inputCount() { return inputs.size(); }

    public boolean isSource() {
        return inputs.isEmpty();
    }

    public boolean isSink() {
        return output == null;
    }

    public boolean isOperator() { return !isSource() && !isSink(); }

    @Override
    public String toString() {
        return "MPE[" + organizationID + "," + templateID + "," + inputs.size() + "," + output + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof ProcessingElementReference otherMPE)) { return false; }
        return organizationID.equals(otherMPE.organizationID)
                && templateID.equals(otherMPE.templateID)
                && inputs.equals(otherMPE.inputs)
                && (Objects.equals(output, otherMPE.output))
                && instanceNumber == otherMPE.instanceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationID, templateID, inputs, output);
    }
}
