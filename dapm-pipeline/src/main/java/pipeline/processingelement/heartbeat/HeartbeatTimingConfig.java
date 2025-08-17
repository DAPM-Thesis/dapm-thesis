package pipeline.processingelement.heartbeat;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HeartbeatTimingConfig {

    private final long sendIntervalMs;
    private final long checkIntervalMs;
    private final long timeoutMs;
    private final long initialGracePeriodMs;
    private final Integer missesThreshold;

    public static HeartbeatTimingConfig getDefaults() {
        return new HeartbeatTimingConfig(3000L, 20000L, 45000L, 60000L, 3);
    }

    @JsonCreator
    public HeartbeatTimingConfig(
            @JsonProperty("sendIntervalMs") long sendIntervalMs,
            @JsonProperty("checkIntervalMs") long checkIntervalMs,
            @JsonProperty("timeoutMs") long timeoutMs,
            @JsonProperty("initialGracePeriodMs") long initialGracePeriodMs, 
            @JsonProperty("missThreshold") Integer missesThreshold) {
        this.sendIntervalMs = sendIntervalMs;
        this.checkIntervalMs = checkIntervalMs;
        this.timeoutMs = timeoutMs;
        this.initialGracePeriodMs = initialGracePeriodMs;
        this.missesThreshold = missesThreshold;
    }

    @JsonProperty("sendIntervalMs")
    public long getSendIntervalMs() { return sendIntervalMs; }

    @JsonProperty("checkIntervalMs")
    public long getCheckIntervalMs() { return checkIntervalMs; }

    @JsonProperty("timeoutMs")
    public long getTimeoutMs() { return timeoutMs; }

    @JsonProperty("initialGracePeriodMs")
    public long getInitialGracePeriodMs() { return initialGracePeriodMs; }

    @JsonProperty("missesThreshold")
    public Integer getMissesThreshold() { return missesThreshold; }
    
    // equals and hashCode for testing and collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeartbeatTimingConfig that = (HeartbeatTimingConfig) o;
        return sendIntervalMs == that.sendIntervalMs && checkIntervalMs == that.checkIntervalMs && timeoutMs == that.timeoutMs && initialGracePeriodMs == that.initialGracePeriodMs && missesThreshold == that.missesThreshold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sendIntervalMs, checkIntervalMs, timeoutMs, initialGracePeriodMs, missesThreshold);
    }
}