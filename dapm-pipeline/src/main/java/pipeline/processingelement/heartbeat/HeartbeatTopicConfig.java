package pipeline.processingelement.heartbeat;

import java.util.List;
import java.util.Collections;

public class HeartbeatTopicConfig {
    private String upstreamHeartbeatPublishTopic;
    private String downstreamHeartbeatPublishTopic;
    private List<String> upstreamNeighborHeartbeatTopicsToSubscribeTo;
    private List<String> downstreamNeighborHeartbeatTopicsToSubscribeTo;

    public HeartbeatTopicConfig() {
        this.upstreamNeighborHeartbeatTopicsToSubscribeTo = Collections.emptyList();
        this.downstreamNeighborHeartbeatTopicsToSubscribeTo = Collections.emptyList();
    }

    // Getters & Setters (as previously defined)
    public String getUpstreamHeartbeatPublishTopic() { return upstreamHeartbeatPublishTopic; }
    public void setUpstreamHeartbeatPublishTopic(String topic) { this.upstreamHeartbeatPublishTopic = topic; }
    public String getDownstreamHeartbeatPublishTopic() { return downstreamHeartbeatPublishTopic; }
    public void setDownstreamHeartbeatPublishTopic(String topic) { this.downstreamHeartbeatPublishTopic = topic; }
    public List<String> getUpstreamNeighborHeartbeatTopicsToSubscribeTo() { return upstreamNeighborHeartbeatTopicsToSubscribeTo; }
    public void setUpstreamNeighborHeartbeatTopicsToSubscribeTo(List<String> topics) { this.upstreamNeighborHeartbeatTopicsToSubscribeTo = topics != null ? topics : Collections.emptyList(); }
    public List<String> getDownstreamNeighborHeartbeatTopicsToSubscribeTo() { return downstreamNeighborHeartbeatTopicsToSubscribeTo; }
    public void setDownstreamNeighborHeartbeatTopicsToSubscribeTo(List<String> topics) { this.downstreamNeighborHeartbeatTopicsToSubscribeTo = topics != null ? topics : Collections.emptyList(); }
}