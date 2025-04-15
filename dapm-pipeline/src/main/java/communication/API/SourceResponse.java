package communication.API;

public record SourceResponse(String templateID, int instanceNumber, String brokerURL, String topic, String instanceID) {
}
