package com.dapm.security_service.services;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class OrgBRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    // In a real app, load this from a config property, e.g., "orgB.peerApi.baseUrl"
    private final String ORG_B_BASE_URL = "http://localhost:8082/api/peer/pipeline-node-requests";

    /**
     * Send a new request to OrgB's PeerApi.
     */
    public PipelineNodeRequest sendRequestToOrgB(PipelineNodeRequest request) {
        return restTemplate.postForObject(ORG_B_BASE_URL, request, PipelineNodeRequest.class);
    }

    /**
     * Poll OrgB's PeerApi for the status of a request.
     */
    public AccessRequestStatus getRequestStatusFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId + "/status";
        return restTemplate.getForObject(url, AccessRequestStatus.class);
    }

    /**
     * Retrieve the entire request record from OrgB, which may include an approvalToken if approved.
     */
    public PipelineNodeRequest getRequestDetailsFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId;
        return restTemplate.getForObject(url, PipelineNodeRequest.class);
    }
}
