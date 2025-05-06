package utils;

import java.util.UUID;

public class IDGenerator { // TODO: delete if not used; probably refactor current usages
    private static int nextID = 0;

    public static int generate() { return nextID++; }

    public static String generateTopic() {
        return "Topic-" + UUID.randomUUID();
    }

    public static String generateInstanceID() {
        return "Instance-" + UUID.randomUUID();
    }

    public static String generateInstanceMetaDataID() {
        return "Instance-MetaData-" + UUID.randomUUID();
    }

    public static String generateConsumerGroupID() {
        return "ConsumerGroup-" + UUID.randomUUID();
    }
}
