package com.dapm.security_service.controllers;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineNodeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peer/pipeline-node-requests")
public class PipelineNodeRequestPeerController {

    @Autowired
    private PipelineNodeRequestRepository requestRepository;

    /**
     * OrgA calls this endpoint to create a request in OrgB's DB.
     */
    @PostMapping
    public PipelineNodeRequest createRequest(@RequestBody PipelineNodeRequest request) {
        // Generate an ID if not provided
        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        request.setStatus(AccessRequestStatus.PENDING);
        return requestRepository.save(request);
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
}
