package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.PipelineDesignDto;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.services.PipelineDesignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipeline/design")
public class PipelineDesignController {

//    private final PipelineDesignService pipelineDesignService;
//
//    public PipelineDesignController(PipelineDesignService pipelineDesignService) {
//        this.pipelineDesignService = pipelineDesignService;
//    }
//
//    /**
//     * GET endpoint to retrieve all available processing element templates for the given organization.
//     */
//    @GetMapping("/available-pe-templates")
//    public ResponseEntity<List<ProcessingElementDto>> getAvailablePeTemplates(@RequestParam String org) {
//        List<ProcessingElementDto> templates = pipelineDesignService.getAvailablePeTemplates(org);
//        return ResponseEntity.ok(templates);
//    }
//
//    /**
//     * POST endpoint to submit a new pipeline design.
//     * Accepts a PipelineDesignDto that contains processing elements and channel configuration.
//     */
//    @PostMapping
//    public ResponseEntity<Pipeline> createPipelineDesign(@RequestBody PipelineDesignDto pipelineDesignDto) {
//        Pipeline createdPipeline = pipelineDesignService.savePipelineDesign(pipelineDesignDto);
//        return ResponseEntity.ok(createdPipeline);
//    }
}
