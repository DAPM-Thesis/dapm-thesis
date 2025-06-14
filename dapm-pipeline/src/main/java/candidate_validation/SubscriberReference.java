package candidate_validation;

import java.util.Objects;

public class SubscriberReference {
    private final ProcessingElementReference element;
    private final int portNumber; // 0-indexed internally, but 1-indexed in API // TODO: should getter return portNumber+1?
    private final boolean isOptional; 

    public SubscriberReference(ProcessingElementReference element, int portNumber, boolean isOptional) {
        assert element != null : "Consumer should not be null";
        assert portNumber > 0 : "Port number should be greater than 0; 0-indexing happens internally";
        this.element = element;
        this.portNumber = portNumber - 1;
        this.isOptional = isOptional;
    }

    public ProcessingElementReference getElement() { return element; }

    public int getPortNumber() { return portNumber; }

    public boolean isOptional() { return isOptional; }

    @Override
    public String toString() {
        return "MCons[" + element + ", " + (portNumber + 1) + ", " + (isOptional ? "optional" : "critical") + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof SubscriberReference mConsOther)) { return false; }
        return element.equals(mConsOther.element) && portNumber == mConsOther.portNumber && isOptional == mConsOther.isOptional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, portNumber);
    }
}
