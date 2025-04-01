package communication;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfiguration {
    // shared
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";

    // Producer
    private static final String KEY_SERIALIZER_CLASS = StringSerializer.class.getName();
    private static final String VALUE_SERIALIZER_CLASS = StringSerializer.class.getName();

    // Consumer
    private static final String KEY_DESERIALIZER_CLASS = StringDeserializer.class.getName();
    private static final String VALUE_DESERIALIZER_CLASS = StringDeserializer.class.getName();
    private static final String GROUP_ID = "consumer-group";
    private static final String AUTO_OFFSET_RESET = "earliest";

    public static Properties getProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER_CLASS);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS);
        return props;
    }


    public static Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KEY_DESERIALIZER_CLASS);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VALUE_DESERIALIZER_CLASS);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        return props;
    }
}
