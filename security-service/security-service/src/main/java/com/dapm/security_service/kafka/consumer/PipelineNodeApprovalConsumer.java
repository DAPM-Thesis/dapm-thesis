package com.dapm.security_service.kafka.consumer;

import com.dapm.security_service.kafka.events.PipelineNodeApprovalEvent;
import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PipelineNodeApprovalConsumer {

    @Autowired
    private PipelineNodeRequestRepository requestRepository;

    @KafkaListener(topics = "pipeline-node-approvals", groupId = "orgA-group")
    public void consumeApprovalEvent(PipelineNodeApprovalEvent event) {
        requestRepository.findById(event.getRequestId()).ifPresent(request -> {
            request.setApprovalToken(event.getApprovalToken());
            // Optionally, update the status if it differs.
            request.setStatus(Enum.valueOf(request.getStatus().getDeclaringClass(), event.getStatus()));
            requestRepository.save(request);
        });
    }
}
