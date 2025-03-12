package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.dtos.PipelineDto;
import com.dapm.security_service.repositories.PipelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    @Autowired
    private PipelineRepository pipelineRepository;

    // READ-ONLY transaction for GET
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PipelineDto getPipelineById(@PathVariable UUID id) {
        return pipelineRepository.findByIdWithNodesAndTokens(id)
                .map(PipelineDto::new)
                .orElse(null);
    }

    // READ-ONLY transaction for GET
    @GetMapping
    @Transactional(readOnly = true)
    public List<PipelineDto> getAllPipelines() {
        return pipelineRepository.findAllWithNodesAndTokens()
                .stream()
                .map(PipelineDto::new)
                .toList();
    }

    @PostMapping
    public PipelineDto createPipeline(@RequestBody Pipeline pipeline) {
        // Set pipeline ID if missing.
        if (pipeline.getId() == null) {
            pipeline.setId(UUID.randomUUID());
        }

        // For each node in the pipeline, set up the many-to-many association.
        if (pipeline.getNodes() != null) {
            pipeline.getNodes().forEach(node -> {
                if (node.getId() == null) {
                    node.setId(UUID.randomUUID());
                }
                // Ensure the node's pipelines collection is initialized.
                if (node.getPipelines() == null) {
                    node.setPipelines(new HashSet<>());
                }
                // Add the current pipeline to the node's pipelines set.
                node.getPipelines().add(pipeline);
            });
        }

        // If tokens are provided (typically generated later), set their IDs.
        if (pipeline.getTokens() != null) {
            pipeline.getTokens().forEach(token -> {
                if (token.getId() == null) {
                    token.setId(UUID.randomUUID());
                }
            });
        }

        Pipeline savedPipeline = pipelineRepository.save(pipeline);
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
