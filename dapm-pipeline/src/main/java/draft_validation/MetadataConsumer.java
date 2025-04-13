package draft_validation;

import java.util.Objects;

public class MetadataConsumer {
    MetadataProcessingElement consumer;
    int portNumber; // 0-indexed internally, but 1-indexed in API

    public MetadataConsumer(MetadataProcessingElement consumer, int portNumber) {
        assert consumer != null : "Consumer should not be null";
        assert portNumber > 0 : "Port number should be greater than 0; 0-indexing happens internally";
        this.consumer = consumer;
        this.portNumber = portNumber - 1;
    }

    @Override
    public String toString() {
        return "MCons[" + consumer + ", " + portNumber + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof MetadataConsumer mConsOther)) { return false; }
        return consumer.equals(mConsOther.consumer) && portNumber == mConsOther.portNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumer, portNumber);
    }
}
