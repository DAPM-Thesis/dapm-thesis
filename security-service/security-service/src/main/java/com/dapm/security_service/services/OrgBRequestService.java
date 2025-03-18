package com.dapm.security_service.services;

import com.dapm.security_service.models.PipelineNodeRequest;
import com.dapm.security_service.models.dtos.peer.PipelineNodeRequestOutboundDto;
import com.dapm.security_service.models.dtos.peer.RequestResponse;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OrgBRequestService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ORG_B_BASE_URL = "http://localhost:8080/api/peer/pipeline-node-requests";

    /**
     * Asynchronously send a new request to OrgB's PeerApi.
     */
    @Async
    public CompletableFuture<RequestResponse> sendRequestToOrgB(PipelineNodeRequestOutboundDto requestDto) {
        RequestResponse response = restTemplate.postForObject(ORG_B_BASE_URL, requestDto, RequestResponse.class);
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Asynchronously poll OrgB's PeerApi for the status of a request.
     */
    @Async
    public CompletableFuture<AccessRequestStatus> getRequestStatusFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId + "/status";
        AccessRequestStatus status = restTemplate.getForObject(url, AccessRequestStatus.class);
        return CompletableFuture.completedFuture(status);
    }

    /**
     * Asynchronously retrieve the entire request record from OrgB.
     */
    @Async
    public CompletableFuture<PipelineNodeRequest> getRequestDetailsFromOrgB(UUID requestId) {
        String url = ORG_B_BASE_URL + "/" + requestId;
        PipelineNodeRequest request = restTemplate.getForObject(url, PipelineNodeRequest.class);
        return CompletableFuture.completedFuture(request);
    }
}
