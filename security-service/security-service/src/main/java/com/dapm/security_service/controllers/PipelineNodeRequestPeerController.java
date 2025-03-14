package com.dapm.security_service.controllers;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.dtos.peer.*;
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
    public RequestResponse createRequest(@RequestBody PipelineNodeRequestOutboundDto requestDto) {
        // Generate an ID if not provided
        if (requestDto.getId() == null) {
            requestDto.setId(UUID.randomUUID());
//
        }
        System.out.println(requestDto.getRequesterInfo()+"i think here is here");

        var request = PipelineNodeRequest.builder()
                .id(requestDto.getId())
                .pipelineNode(nodeRepository.getById(requestDto.getPipelineNodeId()))
                .requesterInfo(convertToRequesterInfo(requestDto.getRequesterInfo()))
                .requestedExecutionCount(0)
                .requestedExecutionCount(0)
                .status(AccessRequestStatus.PENDING)
                .build();
//
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

    private RequesterInfo convertToRequesterInfo(RequesterInfoDto userDto) {
        RequesterInfo info = new RequesterInfo();
        info.setRequesterId(userDto.getRequesterId());
        info.setUsername(userDto.getUsername());
        info.setFaculty(userDto.getFaculty());
        info.setDepartment(userDto.getDepartment());
        info.setOrganization(userDto.getOrganization());
        info.setRole(userDto.getRole());
        info.setPermissions(userDto.getPermissions());

        return info;
    }
}
