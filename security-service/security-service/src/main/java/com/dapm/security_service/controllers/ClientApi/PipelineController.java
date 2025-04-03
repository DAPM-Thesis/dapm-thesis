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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    @Autowired
    private PipelineRepository pipelineRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ProcessingElementRepository processingElementRepository;
    @Autowired
    private TokenRepository tokenRepository;


    @GetMapping
    public List<PipelineDto> getAllPipelines() {
        var pipelines = pipelineRepository.findAll();
        return pipelines.stream().map(PipelineDto::new).toList();
    }

    @GetMapping("/{id}")
    public PipelineDto getPipelineById(@PathVariable UUID id) {
        return pipelineRepository.findById(id).map(PipelineDto::new).orElse(null);
    }

    @PostMapping
    public PipelineDto createPipeline(@RequestBody CreatePipelineDto pipeline) {
        Pipeline p=new Pipeline();
        p.setId(pipeline.getId());
        p.setName(pipeline.getName());
        p.setDescription(pipeline.getDescription());
        p.setCreatedBy(UUID.fromString("11111111-1111-1111-1111-111111111115"));
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p.setChannelsJson(pipeline.getChannelsJson());

        if (pipeline.getId() == null) {
            pipeline.setId(UUID.randomUUID());
        }

        Organization organization = organizationRepository.findByName(pipeline.getOwnerOrganization());
        if (organization == null) {
            throw new IllegalArgumentException("Organization not found");
        }
        p.setOwnerOrganization(organization);

        Set<ProcessingElement> processingElements = pipeline.getProcessingElements().stream()
                .map(processingElementId -> processingElementRepository.findById(processingElementId).orElse(null))
                .filter(Objects::nonNull) // Remove null values if an ID is not found
                .collect(Collectors.toSet());

        p.setProcessingElements(processingElements);

//        Role role = roleRepository.findByName(pipeline.getPipelineRole());
//        if (role == null) {
//            throw new IllegalArgumentException("Organization not found");
//        }
//        p.setPipelineRole(role);

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
