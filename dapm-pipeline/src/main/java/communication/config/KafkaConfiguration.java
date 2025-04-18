package communication.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class KafkaConfiguration {
    // Producer
    private static final String KEY_SERIALIZER_CLASS = StringSerializer.class.getName();
    private static final String VALUE_SERIALIZER_CLASS = StringSerializer.class.getName();

    // Consumer
    private static final String KEY_DESERIALIZER_CLASS = StringDeserializer.class.getName();
    private static final String VALUE_DESERIALIZER_CLASS = StringDeserializer.class.getName();
    private static final String AUTO_OFFSET_RESET = "earliest";

    public static Properties getProducerProperties(String brokerURL) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURL);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER_CLASS);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS);
        return props;
    }

    public static Properties getConsumerProperties(String brokerURL, String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KEY_DESERIALIZER_CLASS);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VALUE_DESERIALIZER_CLASS);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        return props;
    }

    public static Properties getAdminProperties(String brokerURL) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURL);
        return props;
    }
}
