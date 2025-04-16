package pipeline.processingelement;

public record InstanceMetaData(String instanceDetailID,
                              String templateID,
                              String brokerURL,
                              String topic,
                              boolean isProducer,
                              String instanceID) {
}
