package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.CreatePipelineDto;
import com.dapm.security_service.models.dtos.PipelineDto;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.PipelineRepository;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    @Autowired private PipelineRepository pipelineRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private ProcessingElementRepository processingElementRepository;
    @Autowired private TokenRepository tokenRepository;

    @GetMapping
    public List<PipelineDto> getAllPipelines() {
        return pipelineRepository.findAll()
                .stream()
                .map(PipelineDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PipelineDto getPipelineById(@PathVariable UUID id) {
        return pipelineRepository.findById(id).map(PipelineDto::new).orElse(null);
    }

    @PostMapping
    public PipelineDto createPipeline(@RequestBody CreatePipelineDto pipeline) {
        Pipeline p = new Pipeline();
        p.setId(pipeline.getId() != null ? pipeline.getId() : UUID.randomUUID());
        p.setName(pipeline.getName());
        p.setDescription(pipeline.getDescription());
        p.setCreatedBy(UUID.fromString("11111111-1111-1111-1111-111111111115"));
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p.setChannels(pipeline.getChannels());


        Organization organization = organizationRepository.findByName(pipeline.getOwnerOrganization())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        p.setOwnerOrganization(organization);

        Set<ProcessingElement> processingElements = pipeline.getProcessingElements().stream()
                .map(processingElementRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        p.setProcessingElements(processingElements);

        Pipeline savedPipeline = pipelineRepository.save(p);
        return new PipelineDto(savedPipeline);
    }

    @PutMapping("/{id}")
    public PipelineDto updatePipeline(@PathVariable UUID id, @RequestBody Pipeline pipeline) {
        pipeline.setId(id);
        return new PipelineDto(pipelineRepository.save(pipeline));
    }

    @DeleteMapping("/{id}")
    public void deletePipeline(@PathVariable UUID id) {
        pipelineRepository.deleteById(id);
    }
}
