package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.ConfirmationResponse;
import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.dtos.ApproveNodeRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import com.dapm.security_service.services.OrgARequestService;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline-node-requests")
public class PipelineNodeRequestController {

    @Autowired private PipelineNodeRequestRepository requestRepository;
    @Autowired private TokenService tokenService;
    @Autowired private OrgARequestService orgARequestService;

    // Get all pipeline node requests.
    @GetMapping
    public List<PipelineNodeRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    // Get a specific request by its ID.
    @GetMapping("/{id}")
    public PipelineNodeRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }

    // Create a new pipeline node request.
    @PostMapping
    public PipelineNodeRequest createRequest(@RequestBody PipelineNodeRequest request) {
        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        request.setStatus(AccessRequestStatus.PENDING);
        return requestRepository.save(request);
    }

    // Approve a request. This endpoint generates a JWT token with constraints for the approved request.
    @PutMapping("/{id}/approve")
    public PipelineNodeRequest approveRequest(@PathVariable UUID id) {
        PipelineNodeRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        // Generate a token using the requester info and the requested duration.
        // Here, we assume the token's expiration reflects the allowed duration (in milliseconds).
        long expirationMillis = request.getRequestedDurationHours() * 3600000L;
        //String token = tokenService.generateTokenForUser(request.getRequesterInfo(), expirationMillis);
        //request.setApprovalToken(token);
        request.setStatus(AccessRequestStatus.APPROVED);
        request.setDecisionTime(Instant.now());
        return requestRepository.save(request);
    }


    @PostMapping("/approve")
    public String approveNodeRequest(@RequestBody ApproveNodeRequestDto approveNodeRequestDto){
        PipelineNodeRequest request = requestRepository.findById(approveNodeRequestDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(request.getStatus() != AccessRequestStatus.PENDING){
            throw new RuntimeException("Request already processed");
        }

        // convert hr - min - sec - ms
        request.setAllowedDurationHours(((approveNodeRequestDto.getAllowedDurationHours() * 60) * 60) * 1000);
        request.setAllowedNoExecutions(approveNodeRequestDto.getAllowedNoExecutions());
        request.setAllowedDataUsagePercentage(approveNodeRequestDto.getAllowedDataUsagePercentage());

        request.setApprovalToken(tokenService.generateTokenForNodeRequest(request));
        request.setStatus(AccessRequestStatus.APPROVED);

        requestRepository.save(request);

        // sending response to OrgA:
        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(request.getStatus());
        response.setToken(request.getApprovalToken());
        ConfirmationResponse remoteResponse = orgARequestService.sendResponseToOrgA(response);

        // update this part and find a something that Bob should see after approval: could be a 204
        return remoteResponse.isMessageReceived() + "\n "+ request.getApprovalToken();
    }

    // Reject a request.
    @PutMapping("/{id}/reject")
    public PipelineNodeRequest rejectRequest(@PathVariable UUID id) {
        PipelineNodeRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        request.setStatus(AccessRequestStatus.REJECTED);
        request.setDecisionTime(Instant.now());
        return requestRepository.save(request);
    }
}
