package pipeline.processingelement;

public record InstanceMetaData(String instanceDetailID,
                              String templateID,
                              int instanceNumber,
                              String brokerURL,
                              String topic,
                              boolean isProducer,
                              String instanceID) {
}
