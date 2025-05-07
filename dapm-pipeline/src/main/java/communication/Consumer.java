package communication;

import communication.config.ConsumerConfig;
import communication.message.Message;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import utils.IDGenerator;

import java.util.HashMap;
import java.util.Map;


@Component
@Scope("prototype")
public class Consumer {

    private final KafkaListenerEndpointRegistry registry;
    private final ApplicationContext applicationContext;

    private Subscriber<Message> subscriber;
    private int portNumber;
    private String containerId;

    @Autowired
    public Consumer(KafkaListenerEndpointRegistry registry, ApplicationContext applicationContext) {
        this.registry = registry;
        this.applicationContext = applicationContext;
    }

    public void registerListener(Subscriber<Message> subscriber, ConsumerConfig consumerConfig) {
        this.subscriber = subscriber;
        this.portNumber = consumerConfig.portNumber();
        this.containerId = IDGenerator.generateKafkaContainerID();
        MethodKafkaListenerEndpoint<String, String> endpoint = null;
        try {
            endpoint = new MethodKafkaListenerEndpoint<>();
            endpoint.setId(containerId);
            endpoint.setGroupId(IDGenerator.generateConsumerGroupID());
            endpoint.setTopics(consumerConfig.topic());
            endpoint.setBean(this);
            endpoint.setMethod(
                    Consumer.class.getDeclaredMethod("consume", ConsumerRecord.class)
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        endpoint.setMessageHandlerMethodFactory(applicationContext.getBean(MessageHandlerMethodFactory.class));

        KafkaListenerContainerFactory<?> factory = createConsumerContainerFactory(consumerConfig.brokerURL());
        registry.registerListenerContainer(endpoint, factory, false);
    }

    public void consume(ConsumerRecord<String, String> record) {
        Message msg = MessageFactory.deserialize(record.value());
        this.subscriber.observe(msg, portNumber);
    }

    public boolean start() {
        var container = registry.getListenerContainer(containerId);
        if (container == null) {
            throw new IllegalStateException("No listener container found for ID: " + containerId);
        }
        if (!container.isRunning()) {
            container.start();
        }
//        else if (container.isContainerPaused()) {
//            container.resume();
//        }
        return container.isRunning() && !container.isContainerPaused();
    }

    public boolean pause() {
        var container = registry.getListenerContainer(containerId);
        if (container == null) {
            return true;
        }
        container.stop();
        return !container.isRunning();
    }

    // TODO: is pause really needed? heartbeats keep going
//    public boolean pause() {
//        var container = registry.getListenerContainer(containerId);
//        if (container == null) {
//            throw new IllegalStateException("No listener container found for ID: " + containerId);
//        }
//        container.pause();
//        return container.isContainerPaused();
//    }

    public boolean terminate() {
       return pause();
    }

    private KafkaListenerContainerFactory<?> createConsumerContainerFactory(String brokerUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}

