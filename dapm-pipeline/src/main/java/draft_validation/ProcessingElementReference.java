package draft_validation;

import communication.message.Message;
import pipeline.processingelement.ProcessingElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessingElementReference { // TODO: make sure every property is included, including parameterValues
    private final String organizationID;
    private final String organizationHostURL;
    private final String templateID;
    private final List<Class<? extends Message>> inputs;
    private final Class<? extends Message> output;
    private final int instanceNumber;
    private List<Object> parameterValues;
    // TODO: should I make a new constructor that sets parameterValues to an empty list by default or should one always supply the parameterValus list?
    public ProcessingElementReference(String organizationID,
                                      String organizationHostURL,
                                      String templateID,
                                      List<Class<? extends Message>> inputs,
                                      Class<? extends Message> output,
                                      int instanceNumber,
                                      List<Object> parameterValues) {
        assert organizationID != null && templateID != null: "stop being lazy.";
        assert !organizationID.isEmpty() && !templateID.isEmpty() : "indistinguishable orgID and templateID";
        assert instanceNumber > 0 : "instanceID must be positive integer by convention (CFG definition).";

        this.organizationID = organizationID;
        this.organizationHostURL = organizationHostURL;
        this.templateID = templateID;
        this.inputs = inputs;
        this.output = output;
        this.instanceNumber = instanceNumber;
        this.parameterValues = parameterValues;

        assert !(isSource() && isSink()) : "Processing elements must either have inputs or output or both.";
    }

    // with empty parameterValues list
    public ProcessingElementReference(String organizationID,
                                      String organizationHostURL,
                                      String templateID,
                                      List<Class<? extends Message>> inputs,
                                      Class<? extends Message> output,
                                      int instanceNumber) {
        this(organizationID, organizationHostURL, templateID, inputs, output, instanceNumber, new ArrayList<>());
    }

    public String getOrganizationHostURL() { return this.organizationHostURL; }
    public String getTemplateID() { return this.templateID; }
    public int getInstanceNumber() { return this.instanceNumber; }

    /** returns a copy of the inputs */
    public List<Class<? extends Message>> getInputs() { return new ArrayList<>(inputs); }

    public Class<? extends Message> getOutput() { return output; }

    public Class<? extends Message> typeAt(int index) { return inputs.get(index); }

    public int inputCount() { return inputs.size(); }

    public boolean isSource() {
        return inputs == null;
    }

    public boolean isSink() {
        return output == null;
    }

    public boolean isOperator() { return !isSource() && !isSink(); }

    @Override
    public String toString() {
        String inputsString = inputs == null ? "0" : String.valueOf(inputs.size());
        return "MPE[" + organizationID + "," + templateID + "," + inputsString + "," + output + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof ProcessingElementReference otherMPE)) { return false; }
        return organizationID.equals(otherMPE.organizationID)
                && templateID.equals(otherMPE.templateID)
                && organizationHostURL.equals(otherMPE.organizationHostURL)
                && Objects.equals(inputs, otherMPE.inputs)
                && Objects.equals(output, otherMPE.output)
                && instanceNumber == otherMPE.instanceNumber
                && Objects.equals(parameterValues, otherMPE.parameterValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationID, organizationHostURL, templateID, inputs, output, instanceNumber, parameterValues);
    }

    public List<Object> getParameterValues() {
        return parameterValues;
    }
}
