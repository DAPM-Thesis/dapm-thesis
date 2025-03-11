package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Pipeline;
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
    public List<Pipeline> getAllPipelines() {
        return pipelineRepository.findAll();
    }

    @GetMapping("/{id}")
    public Pipeline getPipelineById(@PathVariable UUID id) {
        return pipelineRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Pipeline createPipeline(@RequestBody Pipeline pipeline) {
        if (pipeline.getId() == null) {
            pipeline.setId(UUID.randomUUID());
        }
        return pipelineRepository.save(pipeline);
    }

    @PutMapping("/{id}")
    public Pipeline updatePipeline(@PathVariable UUID id, @RequestBody Pipeline pipeline) {
        pipeline.setId(id);
        return pipelineRepository.save(pipeline);
    }

    @DeleteMapping("/{id}")
    public void deletePipeline(@PathVariable UUID id) {
        pipelineRepository.deleteById(id);
    }
}
