package com.dapm.security_service.services;

import com.dapm.security_service.models.dtos.ProcessingElementDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Component
public class VisiblePeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<ProcessingElementDto> getVisiblePEsFromOrgB(String requestingOrg) {
        String url = UriComponentsBuilder
                .fromHttpUrl("http://orgb:8080/api/peer/visible-pes")
                .queryParam("requestingOrg", requestingOrg)
                .toUriString();

        ProcessingElementDto[] result = restTemplate.getForObject(url, ProcessingElementDto[].class);
        return result != null ? Arrays.asList(result) : List.of();
    }
}
