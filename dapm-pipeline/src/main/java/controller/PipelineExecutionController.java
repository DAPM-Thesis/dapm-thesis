package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.processingelement.ProcessingElement;
import repository.PEInstanceRepository;

@RestController
@RequestMapping("/pipelineExecution")

public class PipelineExecutionController {

    private final PEInstanceRepository peInstanceRepository;

    @Autowired
    public PipelineExecutionController(PEInstanceRepository peInstanceRepository) {
        this.peInstanceRepository = peInstanceRepository;
    }

    @PutMapping("/start/instance/{instanceID}")
    public ResponseEntity<Void> startProcessingElement(@PathVariable("instanceID") String instanceID) {
        ProcessingElement processingElement = peInstanceRepository.getInstance(instanceID);
        if (processingElement != null && processingElement.start())
            { return ResponseEntity.ok().build(); }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/terminate/instance/{instanceID}")
    public ResponseEntity<Void> terminateProcessingElement(@PathVariable("instanceID") String instanceID) {
        ProcessingElement processingElement = peInstanceRepository.getInstance(instanceID);
        if (processingElement != null && processingElement.terminate()) {
                peInstanceRepository.removeInstance(instanceID);
                return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }
}
