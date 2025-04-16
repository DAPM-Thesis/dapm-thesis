package communication.API;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PEInstanceResponse {
    private final String templateID;
    private final int instanceNumber;
    private final String broker;
    private final String topic;
    private final String instanceID;
    private final String instanceMetaDataID;

    @JsonCreator
    public PEInstanceResponse(
            @JsonProperty("templateID") String templateID,
            @JsonProperty("instanceNumber") int instanceNumber,
            @JsonProperty("broker") String broker,
            @JsonProperty("topic") String topic,
            @JsonProperty("instanceID") String instanceID,
            @JsonProperty("instanceMetaDataID") String instanceMetaDataID
    ) {
        this.templateID = templateID;
        this.instanceNumber = instanceNumber;
        this.broker = broker;
        this.topic = topic;
        this.instanceID = instanceID;
        this.instanceMetaDataID = instanceMetaDataID;
    }

    public static class Builder {
        private final String templateID;
        private final int instanceNumber;
        private String broker;
        private String topic;
        private String instanceID;
        private String instanceMetaDataID;

        public Builder(String templateID, int instanceNumber) {
            this.templateID = templateID;
            this.instanceNumber = instanceNumber;
        }

        public Builder broker(String broker) {
            this.broker = broker;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder instanceID(String instanceID) {
            this.instanceID = instanceID;
            return this;
        }

        public Builder instanceMetaDataID(String instanceMetaDataID) {
            this.instanceMetaDataID = instanceMetaDataID;
            return this;
        }

        public PEInstanceResponse build() {
            return new PEInstanceResponse(templateID, instanceNumber, broker, topic, instanceID, instanceMetaDataID);
        }
    }

    public String getTemplateID() {
        return templateID;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public String getBroker() {
        return broker;
    }

    public String getTopic() {
        return topic;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getInstanceMetaDataID() {
        return instanceMetaDataID;
    }
}
