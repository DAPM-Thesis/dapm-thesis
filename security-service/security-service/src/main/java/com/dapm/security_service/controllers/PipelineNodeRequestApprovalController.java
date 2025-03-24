package com.dapm.security_service.controllers;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.dtos.ApproveNodeRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.kafka.events.PipelineNodeApprovalEvent;
import com.dapm.security_service.kafka.producer.PipelineNodeApprovalProducer;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/pipeline-node-requests")
public class PipelineNodeRequestApprovalController {

    @Autowired
    private PipelineNodeRequestRepository requestRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PipelineNodeApprovalProducer approvalProducer;

    /**
     * Bob (Department Head in OrgB) approves a pipeline node request.
     * It generates a JWT token with constraints, updates the request record,
     * and publishes a Kafka event so that OrgA can update its local copy.
     */
    @PostMapping("/approve")
    public RequestResponse approveNodeRequest(@RequestBody ApproveNodeRequestDto approveDto) {
        PipelineNodeRequest request = requestRepository.findById(approveDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        // Set constraints based on the DTO (convert hours to milliseconds as needed)
        // Here, assume approveDto provides allowedDurationHours, allowedNoExecutions, and allowedDataUsagePercentage.
        // For token generation, we assume that these fields are stored in the request.
        request.setRequestedDurationHours(approveDto.getAllowedDurationHours());
        request.setRequestedExecutionCount(approveDto.getAllowedNoExecutions());
        // Suppose you add a new field for data usage in your entity; see previous update.
        request.setAllowedDataUsagePercentage(approveDto.getAllowedDataUsagePercentage());

        // Generate JWT token for the request.
        // For token expiration, we use allowedDurationHours (converted to ms).
        long expirationMillis = approveDto.getAllowedDurationHours() * 3600000L;
        String token = tokenService.generateTokenForNodeRequest(request);

        request.setApprovalToken(token);
        request.setStatus(AccessRequestStatus.APPROVED);
        request.setDecisionTime(Instant.now());

        requestRepository.save(request);

        // Publish a Kafka event so OrgA can update its local record.
        PipelineNodeApprovalEvent approvalEvent = new PipelineNodeApprovalEvent(
                request.getId(), token, request.getStatus().name()
        );
        approvalProducer.sendApprovalEvent(approvalEvent);

        // Build response DTO
        RequestResponse response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(request.getStatus());
        response.setToken(token);
        return response;
    }
}
