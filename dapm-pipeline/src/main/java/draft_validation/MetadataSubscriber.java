package draft_validation;

import java.util.Objects;

public class MetadataSubscriber {
    private final MetadataProcessingElement element;
    private final int portNumber; // 0-indexed internally, but 1-indexed in API

    public MetadataSubscriber(MetadataProcessingElement element, int portNumber) {
        assert element != null : "Consumer should not be null";
        assert portNumber > 0 : "Port number should be greater than 0; 0-indexing happens internally";
        this.element = element;
        this.portNumber = portNumber - 1;
    }

    public MetadataProcessingElement getElement() { return element; }

    public int getPortNumber() { return portNumber; }

    @Override
    public String toString() {
        return "MCons[" + element + ", " + portNumber + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof MetadataSubscriber mConsOther)) { return false; }
        return element.equals(mConsOther.element) && portNumber == mConsOther.portNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, portNumber);
    }
}
