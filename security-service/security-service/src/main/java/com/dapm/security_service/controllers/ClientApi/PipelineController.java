package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.dtos.PipelineDto;
import com.dapm.security_service.repositories.PipelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    @Autowired
    private PipelineRepository pipelineRepository;

    @GetMapping
    public List<PipelineDto> getAllPipelines() {
        var pipelines =  pipelineRepository.findAll();
        return pipelines.stream().map(PipelineDto::new).toList();
    }

    @GetMapping("/{id}")
    public PipelineDto getPipelineById(@PathVariable UUID id) {
        return pipelineRepository.findById(id).map(PipelineDto::new).orElse(null);
    }

    @PostMapping
    public PipelineDto createPipeline(@RequestBody Pipeline pipeline) {
        if (pipeline.getId() == null) {
            pipeline.setId(UUID.randomUUID());
        }

        if (pipeline.getNodes() != null) {
            pipeline.getNodes().forEach(node -> {
                if (node.getId() == null) {
                    node.setId(UUID.randomUUID());
                }
            });
        }

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
