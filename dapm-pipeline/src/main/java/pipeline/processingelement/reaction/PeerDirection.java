package pipeline.processingelement.reaction;

public enum PeerDirection {
    UPSTREAM_PRODUCER,      // A PE that produces data to the current PE 
    DOWNSTREAM_CONSUMER,    // A PE that consumes data from the current PE
}
