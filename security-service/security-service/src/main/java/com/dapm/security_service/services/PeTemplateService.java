package com.dapm.security_service.services;

import com.dapm.security_service.models.PeTemplate;
import com.dapm.security_service.models.dtos.PeTemplateDto;
import com.dapm.security_service.repositories.PeTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Make sure this import is here!
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeTemplateService {

    private final PeTemplateRepository peTemplateRepository;
    private final RestTemplate restTemplate;

    // Inject the URL from application.properties
    @Value("${orgb.peer-api.url}")
    private String orgBPeerApiUrl;

    @Autowired
    public PeTemplateService(PeTemplateRepository peTemplateRepository, RestTemplate restTemplate) {
        this.peTemplateRepository = peTemplateRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns the list of PE templates available to the given organization.
     * It fetches:
     *  - Local PE templates owned by the organization.
     *  - External PE templates from OrgB that are marked visible to the organization.
     *
     * @param org the organization name (e.g., "OrgA")
     * @return List of PeTemplateDto combining local and external templates.
     */
    public List<PeTemplateDto> getAvailablePeTemplates(String org) {
        // Fetch local PE templates for the organization (e.g., OrgA)
        List<PeTemplateDto> localTemplates = peTemplateRepository.findByOwner(org)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Fetch external PE templates from OrgB's Peer API
        try {
            ResponseEntity<PeTemplateDto[]> responseEntity =
                    restTemplate.getForEntity(orgBPeerApiUrl + org, PeTemplateDto[].class);
            PeTemplateDto[] externalTemplates = responseEntity.getBody();
            if (externalTemplates != null) {
                localTemplates.addAll(List.of(externalTemplates));
            }
        } catch (Exception e) {
            System.err.println("Error fetching external PE templates: " + e.getMessage());
        }

        return localTemplates;
    }

    /**
     * Helper method to map a PeTemplate entity to its corresponding DTO.
     *
     * @param template the PeTemplate entity.
     * @return the mapped PeTemplateDto.
     */
    private PeTemplateDto convertToDto(PeTemplate template) {
        return new PeTemplateDto(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getOwner(),
                template.getVisibility()
        );
    }
}
