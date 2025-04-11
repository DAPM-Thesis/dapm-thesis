package draft_validation;

import java.util.Objects;

public class MetadataChannel {
    private final MetadataProcessingElement from;
    private final MetadataProcessingElement to;

    public MetadataChannel(MetadataProcessingElement from, MetadataProcessingElement to) {
        this.from = from;
        this.to = to;
    }

    public MetadataProcessingElement fromElement() { return from; }
    public MetadataProcessingElement toElement() { return to; }

    @Override
    public String toString() {
        return "MC[" + from + ", " + to + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MetadataChannel otherChannel)) return false;
        return from.equals(otherChannel.from) && to.equals(otherChannel.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
