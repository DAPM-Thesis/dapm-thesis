package communication.API;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import communication.config.ProducerConfig;


public class PEInstanceResponse {
    private final String templateID;
    private final String instanceID;
    private final ProducerConfig producerConfig;
    private String tokenValue;
    
    @JsonCreator
    public PEInstanceResponse(
            @JsonProperty("templateID") String templateID,
            @JsonProperty("instanceID") String instanceID,
            @JsonProperty("producerConfig") ProducerConfig producerConfig,
            @JsonProperty("tokenValue") String tokenValue) {            
        this.templateID = templateID;
        this.instanceID = instanceID;
        this.producerConfig = producerConfig;
        this.tokenValue = tokenValue;
    }

    public static class Builder {
        private final String templateID;
        private final String instanceID;
        private ProducerConfig producerConfig;
        private String tokenValue;

        public Builder(String templateID, String instanceID) {
            this.templateID = templateID;
            this.instanceID = instanceID;
        }

        public Builder producerConfig(ProducerConfig config) {
            this.producerConfig = config;
            return this;
        }

        public Builder tokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public PEInstanceResponse build() {
            return new PEInstanceResponse(templateID, instanceID, producerConfig, tokenValue);
        }
    }

    public String getTemplateID() {
        return templateID;
    }

    public ProducerConfig getProducerConfig() {return producerConfig;}

    public String getInstanceID() {
        return instanceID;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }
}
