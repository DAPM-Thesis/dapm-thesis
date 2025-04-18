package draft_validation;

import java.util.Set;

public record PipelineDraft(Set<ProcessingElementReference> elements, Set<ChannelReference> channels) {
}
