package com.dapm.security_service.kafka.producer;

import com.dapm.security_service.kafka.events.PipelineNodeRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PipelineNodeRequestProducer {

    private final KafkaTemplate<String, PipelineNodeRequestEvent> kafkaTemplate;

    @Autowired
    public PipelineNodeRequestProducer(KafkaTemplate<String, PipelineNodeRequestEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRequestEvent(PipelineNodeRequestEvent event) {
        kafkaTemplate.send("pipeline-node-requests", event.getRequestId().toString(), event);
    }
}
