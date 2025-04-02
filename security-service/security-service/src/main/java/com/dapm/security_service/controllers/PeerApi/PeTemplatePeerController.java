package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.models.dtos.PeTemplateDto;
import com.dapm.security_service.models.PeTemplate;
import com.dapm.security_service.repositories.PeTemplateRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/peer/api/pe-templates")
public class PeTemplatePeerController {

    private final PeTemplateRepository peTemplateRepository;

    public PeTemplatePeerController(PeTemplateRepository peTemplateRepository) {
        this.peTemplateRepository = peTemplateRepository;
    }

    /**
     * Endpoint for OrgB's peer API: returns a list of PE templates owned by OrgB that are visible to the requesting org.
     *
     * @param org the organization requesting the templates (e.g., "OrgA")
     * @return a list of PeTemplateDto objects
     */
    @GetMapping
    public List<PeTemplateDto> getPeTemplatesForPeer(@RequestParam String org) {
        return peTemplateRepository.findAll().stream()
                .filter(template -> "OrgB".equals(template.getOwner()) && template.getVisibility().contains(org))
                .map(template -> new PeTemplateDto(
                        template.getId(),
                        template.getName(),
                        template.getDescription(),
                        template.getOwner(),
                        template.getVisibility()))
                .collect(Collectors.toList());
    }
}
