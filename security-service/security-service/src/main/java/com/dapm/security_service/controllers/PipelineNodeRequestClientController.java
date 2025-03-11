package com.dapm.security_service.controllers;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.services.OrgBRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/client/pipeline-node-requests")
public class PipelineNodeRequestClientController {

    @Autowired
    private OrgBRequestService orgBRequestService;

    /**
     * Alice calls this to request use of a node owned by OrgB.
     * We forward the request to OrgB's PeerApi.
     */
    @PostMapping
    public PipelineNodeRequest createRequest(@RequestBody PipelineNodeRequest request) {
        // We do not store it locally in OrgA's DB;
        // Instead we forward to OrgB's PeerApi and return the created request.
        return orgBRequestService.sendRequestToOrgB(request);
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
}
