package com.dapm.security_service.controllers;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.dtos.PipelineNodeRequestDto;
import com.dapm.security_service.models.dtos.peer.PipelineNodeRequestOutboundDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.dtos.peer.RequesterInfoDto;
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
    public RequestResponse initiatePeerRequest(@RequestBody PipelineNodeRequestDto requestDto) {
        PipelineNodeRequest request = convertDtoToEntity(requestDto);

        // 2. Save locally
        PipelineNodeRequest localRequest = pipelineNodeRequestRepository.save(request);

        // 3. Convert entity → outbound DTO
        PipelineNodeRequestOutboundDto outboundDto = toOutboundDto(localRequest);


        // 4. Send the outbound DTO to OrgB
        // (orgBRequestService should accept the outbound DTO instead of the entity)
        RequestResponse remoteResponseDto = orgBRequestService.sendRequestToOrgB(outboundDto);

//        // Update the local record with any details returned from OrgB (e.g., approval token, updated status).
//        localRequest.setApprovalToken(remoteResponseDto.getApprovalToken());
//        localRequest.setStatus(remoteResponseDto.getStatus());
//        localRequest.setDecisionTime(remoteResponseDto.getDecisionTime());
//        localRequest = pipelineNodeRequestRepository.save(localRequest);

        // Return the updated local record.
        return remoteResponseDto;
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
//        requester.setRole(String.valueOf(user.getRoles().stream().findFirst().orElse(null)));

        String firstRoleName = user.getRoles()
                .stream()
                .findFirst()
                .map(r -> r.getName())  // or just Role::getName
                .orElse(null);

        requester.setRole(firstRoleName);

        requester.setPermissions("");

        return PipelineNodeRequest.builder()
                .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
                .pipelineNode(node)
                .requesterInfo(requester)
                .pipelineId(dto.getPipelineId())
                .requestedExecutionCount(dto.getRequestedExecutionCount())
                .requestedDurationHours(dto.getRequestedDurationHours())
                .status(dto.getStatus())
                .approvalToken(dto.getApprovalToken())
                .decisionTime(dto.getDecisionTime())
                .build();
    }
    private PipelineNodeRequestOutboundDto toOutboundDto(PipelineNodeRequest entity) {
        PipelineNodeRequestOutboundDto dto = new PipelineNodeRequestOutboundDto();

        // 1) Top-level fields
        dto.setId(entity.getId());
        dto.setRequestedExecutionCount(entity.getRequestedExecutionCount());
        dto.setRequestedDurationHours(entity.getRequestedDurationHours());
        dto.setStatus(entity.getStatus());
        dto.setApprovalToken(entity.getApprovalToken());
        dto.setDecisionTime(entity.getDecisionTime());

        dto.setPipelineId(entity.getPipelineId());

        // 2) PipelineNode ID
        if (entity.getPipelineNode() != null) {
            dto.setPipelineNodeId(entity.getPipelineNode().getId());
        }

        // 3) RequesterInfo
        if (entity.getRequesterInfo() != null) {
            RequesterInfoDto infoDto = new RequesterInfoDto();
            infoDto.setRequesterId(entity.getRequesterInfo().getRequesterId());
            infoDto.setUsername(entity.getRequesterInfo().getUsername());
            infoDto.setOrganization(entity.getRequesterInfo().getOrganization());
            infoDto.setFaculty(entity.getRequesterInfo().getFaculty());
            infoDto.setDepartment(entity.getRequesterInfo().getDepartment());
            infoDto.setRole(entity.getRequesterInfo().getRole());
            infoDto.setPermissions(entity.getRequesterInfo().getPermissions());
            dto.setRequesterInfo(infoDto);
        }

        return dto;
    }

}
