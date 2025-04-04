package draft_validation;

public class MetadataChannel {
    private final MetadataProcessingElement from;
    private final MetadataProcessingElement to;

    public MetadataChannel(MetadataProcessingElement from, MetadataProcessingElement to) {
        this.from = from;
        this.to = to;
    }
}
