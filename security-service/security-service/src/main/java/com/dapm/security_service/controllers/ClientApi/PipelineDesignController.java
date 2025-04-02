package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.PeTemplateDto;
import com.dapm.security_service.models.dtos.PeTemplateDto;
import com.dapm.security_service.services.PeTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipeline/design")
public class PipelineDesignController {

    private final PeTemplateService peTemplateService;

    public PipelineDesignController(PeTemplateService peTemplateService) {
        this.peTemplateService = peTemplateService;
    }

    /**
     * Endpoint to retrieve all available PE templates for the given organization.
     * It aggregates local templates from OrgA and external templates from OrgB (if visible).
     *
     * @param org the name of the organization (e.g., "OrgA")
     * @return a combined list of available PE templates as JSON.
     */
    @GetMapping("/available-pe-templates")
    public ResponseEntity<List<PeTemplateDto>> getAvailablePeTemplates(@RequestParam String org) {
        List<PeTemplateDto> templates = peTemplateService.getAvailablePeTemplates(org);
        return ResponseEntity.ok(templates);
    }
}
