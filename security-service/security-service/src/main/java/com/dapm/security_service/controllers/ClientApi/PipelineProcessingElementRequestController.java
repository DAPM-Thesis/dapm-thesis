package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.ConfirmationResponse;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.dtos.ApproveProcessingElementRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.services.OrgARequestService;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline-node-requests")
public class PipelineProcessingElementRequestController {

    @Autowired private PipelineProcessingElementRequestRepository requestRepository;
    @Autowired private TokenService tokenService;
    @Autowired private OrgARequestService orgARequestService;

    // Get all pipeline node requests.
    @GetMapping
    public List<PipelineProcessingElementRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    // Get a specific request by its ID.
    @GetMapping("/{id}")
    public PipelineProcessingElementRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }

    // Create a new pipeline node request.
    @PostMapping
    public PipelineProcessingElementRequest createRequest(@RequestBody PipelineProcessingElementRequest request) {
        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        request.setStatus(AccessRequestStatus.PENDING);
        return requestRepository.save(request);
    }

    // Approve a request. This endpoint generates a JWT token with constraints for the approved request.
    @PutMapping("/{id}/approve")
    public PipelineProcessingElementRequest approveRequest(@PathVariable UUID id) {
        PipelineProcessingElementRequest request = requestRepository.findById(id)
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
    public String approveNodeRequest(@RequestBody ApproveProcessingElementRequestDto approveNodeRequestDto){
        PipelineProcessingElementRequest request = requestRepository.findById(approveNodeRequestDto.getRequestId())
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

        // Send notification to the webhook URL provided in the request
        String webhookUrl = request.getWebhookUrl(); // Assuming the webhook URL is stored in the request entity
        System.out.println(webhookUrl+" mmmmmmmmmmmmmmm");
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            // Prepare the data to send to the webhook
            RequestResponse webhookResponse = new RequestResponse();
            webhookResponse.setRequestId(request.getId());
            webhookResponse.setRequestStatus(request.getStatus());
            webhookResponse.setToken(request.getApprovalToken());

            // Use RestTemplate to send the notification to the webhook
            RestTemplate restTemplate = new RestTemplate();
            try {
                // Send a POST request to the webhook URL with the response data
                ResponseEntity<String> webhookResponseEntity = restTemplate.exchange(
                        webhookUrl,
                        HttpMethod.POST,
                        new org.springframework.http.HttpEntity<>(webhookResponse),
                        String.class
                );

                // Log or handle the webhook response (optional)
                System.out.println("Org B-- Webhook response: " + webhookResponseEntity.getBody());

            } catch (Exception e) {
                // Handle errors with the webhook request (e.g., logging, retrying)
                System.err.println("Error sending webhook notification: " + e.getMessage());
            }
        }


        // update this part and find a something that Bob should see after approval: could be a 204
        return remoteResponse.isMessageReceived() + "\n "+ request.getApprovalToken();
    }


    // Reject a request.
    @PutMapping("/{id}/reject")
    public PipelineProcessingElementRequest rejectRequest(@PathVariable UUID id) {
        PipelineProcessingElementRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        request.setStatus(AccessRequestStatus.REJECTED);
        request.setDecisionTime(Instant.now());
        return requestRepository.save(request);
    }
}
