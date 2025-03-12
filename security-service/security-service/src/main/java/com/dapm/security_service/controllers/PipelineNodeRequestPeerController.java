package com.dapm.security_service.controllers;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.dtos.peer.UserDto;
import com.dapm.security_service.models.dtos.peer.PipelineNodeRequestDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.NodeRepository;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peer/pipeline-node-requests")
public class PipelineNodeRequestPeerController {

    @Autowired private PipelineNodeRequestRepository requestRepository;
    @Autowired private NodeRepository nodeRepository;

    /**
     * OrgA calls this endpoint to create a request in OrgB's DB.
     */
    @PostMapping
    public RequestResponse createRequest(@RequestBody PipelineNodeRequestDto requestDto) {
        // Generate an ID if not provided
        if (requestDto.getRequestId() == null) {
            requestDto.setRequestId(UUID.randomUUID());
        }

        var request = PipelineNodeRequest.builder()
                .id(requestDto.getRequestId())
                .pipelineNode(nodeRepository.getById(requestDto.getPipelineNodeId()))
                .requesterInfo(convertToRequesterInfo(requestDto.getRequester()))
                .requestedExecutionCount(0)
                .requestedExecutionCount(0)
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
    public PipelineNodeRequest getRequestById(@PathVariable UUID id) {
        return requestRepository.findById(id).orElse(null);
    }

    /**
     * OrgA can call this to just get the status (if it doesn't want the entire request object).
     */
    @GetMapping("/{id}/status")
    public AccessRequestStatus getRequestStatus(@PathVariable UUID id) {
        PipelineNodeRequest req = requestRepository.findById(id).orElse(null);
        return (req == null) ? null : req.getStatus();
    }

    private RequesterInfo convertToRequesterInfo(UserDto userDto) {
        RequesterInfo info = new RequesterInfo();
        info.setRequesterId(UUID.fromString(userDto.getId()));
        info.setUsername(userDto.getUsername());
        info.setFaculty(userDto.getFaculty());
        info.setDepartment(userDto.getDepartment());
        info.setOrganization(userDto.getOrganization());
        info.setRole(userDto.getRole());
        info.setPermissions(userDto.getPermissions());

        return info;
    }
}
