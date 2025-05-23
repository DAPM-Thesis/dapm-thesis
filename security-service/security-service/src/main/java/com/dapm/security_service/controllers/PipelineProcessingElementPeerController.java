
package com.dapm.security_service.controllers;

import com.dapm.security_service.models.ConfirmationResponse;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.dtos.peer.*;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peer/pipeline-node-requests")
public class PipelineProcessingElementPeerController {

    @Autowired private PipelineProcessingElementRequestRepository requestRepository;
    @Autowired private ProcessingElementRepository processingElementRepository;

    /**
     * OrgA calls this endpoint to create a request in OrgB's DB.
     */
    @PostMapping
    public RequestResponse createRequest(@RequestBody PipelineProcessingElementRequestOutboundDto requestDto) {
        // Generate an ID if not provided
        if (requestDto.getId() == null) {
            requestDto.setId(UUID.randomUUID());
        }

        var request = PipelineProcessingElementRequest.builder()
                .id(requestDto.getId())
                .pipelineId(requestDto.getPipelineId())
                .pipelineNode(processingElementRepository.getById(requestDto.getPipelinePeId()))
                .requesterInfo(convertToRequesterInfo(requestDto.getRequesterInfo()))
                .requestedExecutionCount(0)
                .requestedExecutionCount(0)
                .webhookUrl(requestDto.getWebhookUrl())
                .status(AccessRequestStatus.PENDING)
                .build();

        var savedRequest = requestRepository.save(request);

        var response = new RequestResponse();
        response.setRequestId(request.getId());
        response.setRequestStatus(AccessRequestStatus.PENDING);
        response.setToken("");

        return response;

    }

    /**
     * OrgA calls this endpoint to retrieve the entire request record (including the token if approved).
     */
    @GetMapping("/{id}")
    public PipelineProcessingElementRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }

    /**
     * OrgA can call this to just get the status (if it doesn't want the entire request object).
     */
    @GetMapping("/{id}/status")
    public AccessRequestStatus getRequestStatus(@PathVariable UUID id) {
        PipelineProcessingElementRequest req = requestRepository.findById(id).orElse(null);
        return (req == null) ? null : req.getStatus();
    }

    // OrgB call this endpoint to send approval of the request.
    @PostMapping("/approve")
    public ConfirmationResponse approveRequest(@RequestBody RequestResponse requestResponse){
        var request = requestRepository.getById(requestResponse.getRequestId());

        if(request.getId() == null){
            var confirmationRespone = new ConfirmationResponse();
            confirmationRespone.setMessageReceived(false);
            return confirmationRespone;
        }

        request.setApprovalToken(requestResponse.getToken());
        request.setStatus(requestResponse.getRequestStatus());

        requestRepository.save(request);

        var confirmationResponse = new ConfirmationResponse();
        confirmationResponse.setMessageReceived(true);

        return confirmationResponse;
    }

    private RequesterInfo convertToRequesterInfo(RequesterInfoDto userDto) {
        RequesterInfo info = new RequesterInfo();
        info.setRequesterId(userDto.getRequesterId());
        info.setUsername(userDto.getUsername());
        info.setOrganization(userDto.getOrganization());
        info.setRole(userDto.getRole());
        info.setPermissions(userDto.getPermissions());

        return info;
    }
}