package com.dapm.security_service.controllers;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.dtos.PipelineNodeRequestDto;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.NodeRepository;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.dapm.security_service.services.OrgBRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/client/pipeline-node-requests")
public class PipelineNodeRequestClientController {

    @Autowired private OrgBRequestService orgBRequestService;

    @Autowired private PipelineNodeRequestRepository pipelineNodeRequestRepository;
    @Autowired private NodeRepository nodeRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Alice calls this to request use of a node owned by OrgB.
     * We forward the request to OrgB's PeerApi.
     */
    @PostMapping("/peer")
    public PipelineNodeRequest initiatePeerRequest(@RequestBody PipelineNodeRequestDto requestDto) {
        PipelineNodeRequest request = convertDtoToEntity(requestDto);

        // Ensure the request has a unique ID.
        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        // Set the request status to PENDING.
        request.setStatus(AccessRequestStatus.PENDING);

        // Store the request locally in OrgA's database.
        PipelineNodeRequest localRequest = pipelineNodeRequestRepository.save(request);

        // Forward the request to OrgB's PeerApi.
        PipelineNodeRequest remoteResponse = orgBRequestService.sendRequestToOrgB(request);

        // Update the local record with any details returned from OrgB (e.g., approval token, updated status).
        localRequest.setApprovalToken(remoteResponse.getApprovalToken());
        localRequest.setStatus(remoteResponse.getStatus());
        localRequest.setDecisionTime(remoteResponse.getDecisionTime());
        localRequest = pipelineNodeRequestRepository.save(localRequest);

        // Return the updated local record.
        return localRequest;
    }

    /**
     * Check the status of a request that was sent to OrgB.
     * This method calls OrgB's PeerApi to retrieve the latest status.
     */
    @GetMapping("/{id}/status")
    public AccessRequestStatus getRequestStatus(@PathVariable UUID id) {
        return orgBRequestService.getRequestStatusFromOrgB(id);
    }

    /**
     * Get the final approved request record from OrgB (which may contain the JWT token).
     */
    @GetMapping("/{id}/details")
    public PipelineNodeRequest getRequestDetails(@PathVariable UUID id) {
        return orgBRequestService.getRequestDetailsFromOrgB(id);
    }

    private PipelineNodeRequest convertDtoToEntity(PipelineNodeRequestDto dto) {
        Node node = nodeRepository.findById(dto.getPipelineNodeId())
                .orElseThrow(() -> new RuntimeException("Node not found: " + dto.getPipelineNodeId()));
        User user = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found: " + dto.getRequesterId()));

        RequesterInfo requester = new RequesterInfo();
        requester.setRequesterId(user.getId());
        requester.setUsername(user.getUsername());
        requester.setOrganization(user.getOrganization().getName());
        requester.setFaculty(user.getFaculty().getName());
        requester.setDepartment(user.getDepartment().getName());
        requester.setRole(String.valueOf(user.getRoles().stream().findFirst().orElse(null)));
        requester.setPermissions("");

        return PipelineNodeRequest.builder()
                .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
                .pipelineNode(node)
                .requesterInfo(requester)
                .requestedExecutionCount(dto.getRequestedExecutionCount())
                .requestedDurationHours(dto.getRequestedDurationHours())
                .status(dto.getStatus())
                .approvalToken(dto.getApprovalToken())
                .decisionTime(dto.getDecisionTime())
                .build();
    }
}
