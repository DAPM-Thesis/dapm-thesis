package pipeline.processingelement.reaction;

import java.util.Set;
import java.util.Collections;

public record FaultContext(
    PeerDirection affectedPeerDirection,
    Set<String> silentMonitoredTopics,              // Specific topics that went silent for this direction
    Set<String> allConfiguredTopicsForDirection     // All topics PE was monitoring for this direction
) {
    public FaultContext {
        silentMonitoredTopics = silentMonitoredTopics != null 
            ? Collections.unmodifiableSet(silentMonitoredTopics) 
            : Collections.emptySet();
        
        allConfiguredTopicsForDirection = allConfiguredTopicsForDirection != null 
            ? Collections.unmodifiableSet(allConfiguredTopicsForDirection) 
            : Collections.emptySet();
    }
}
