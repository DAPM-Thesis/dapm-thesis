package pipeline.heartbeat;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;

import communication.message.impl.HeartbeatID;

/** 
 * Strategy: how to decide the PE is still alive. 
 */
@FunctionalInterface
public interface VerificationStrategy {
    boolean verify(Map<HeartbeatID, Instant> lastSeen, Instant now);

    static VerificationStrategy allWithin(Duration maxGap) {
        return (map, now) -> map.values().stream()
                .allMatch(t -> !t.plus(maxGap).isBefore(now));
    }

    static VerificationStrategy anyWithin(Duration maxGap) {
        return (map, now) -> map.values().stream()
                .anyMatch(t -> !t.plus(maxGap).isBefore(now));
    }
}