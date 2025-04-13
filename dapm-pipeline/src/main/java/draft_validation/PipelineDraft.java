package draft_validation;

import java.util.Set;

public record PipelineDraft(Set<MetadataProcessingElement> elements, Set<MetadataChannel> channels) {
}
