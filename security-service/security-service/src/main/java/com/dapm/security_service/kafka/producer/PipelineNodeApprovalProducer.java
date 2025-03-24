package com.dapm.security_service.kafka.producer;

import com.dapm.security_service.kafka.events.PipelineNodeApprovalEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PipelineNodeApprovalProducer {

    private final KafkaTemplate<String, PipelineNodeApprovalEvent> kafkaTemplate;

    @Autowired
    public PipelineNodeApprovalProducer(KafkaTemplate<String, PipelineNodeApprovalEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendApprovalEvent(PipelineNodeApprovalEvent event) {
        // Use requestId as the key
        kafkaTemplate.send("pipeline-node-approvals", event.getRequestId().toString(), event);
    }
}
