package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.dtos.ConfigureValidationDto;
import com.dapm.security_service.models.dtos.MissingPermissionsDto;
import com.dapm.security_service.repositories.PipelineRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.repositories.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/configure-pipeline")
public class ConfigurePipelineController {
    @Autowired
    private PipelineRepository pipelineRepository;

//    @PreAuthorize("hasAuthority('CONFIGURE_PIPELINE:' + @pipelineRepository.findByName(#pipelineName).get().project.name)")
    @GetMapping("/{pipelineName}/validate")
    public ConfigureValidationDto validatePipeline(@PathVariable String pipelineName) {
        var pipeline = pipelineRepository.findByName(pipelineName)
                .orElseThrow(() -> new RuntimeException("Pipeline '" + pipelineName + "' not found"));

        // Collect partner-owned processing elements with their org names
        var partnerElements = pipeline.getProcessingElements().stream()
                .filter(pe -> pe.getOwnerPartnerOrganization() != null)
                .map(pe -> new MissingPermissionsDto(
                        pe.getTemplateId(),
                        pe.getOwnerPartnerOrganization().getName()))
                .toList();


        ConfigureValidationDto validationDto = new ConfigureValidationDto();
        if (partnerElements.isEmpty()) {
            validationDto.setStatus("VALID");
            validationDto.setMissingPermissions(List.of());
        } else {
            validationDto.setStatus("INVALID");
            validationDto.setMissingPermissions(partnerElements);
        }

        return validationDto;
    }
}
