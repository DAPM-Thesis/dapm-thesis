package pipeline.accesscontrolled.processingelement;

public record ChannelConfiguration(
        String dataTopic,
        String hbUpTopic,
        String hbDownTopic,
        String brokerURL
) {}
