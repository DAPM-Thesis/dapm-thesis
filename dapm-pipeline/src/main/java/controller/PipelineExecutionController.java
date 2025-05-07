package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.processingelement.ProcessingElement;
import repository.PEInstanceRepository;
import utils.LogUtil;

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
        if (processingElement != null) {
            boolean started = processingElement.start();
            if (started) return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/pause/instance/{instanceID}")
    public ResponseEntity<Void> stopProcessingElement(@PathVariable("instanceID") String instanceID) {
        ProcessingElement processingElement = peInstanceRepository.getInstance(instanceID);
        if (processingElement != null) {
            boolean paused = processingElement.pause();
            if (paused) return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/terminate/instance/{instanceID}")
    public ResponseEntity<Void> terminateProcessingElement(@PathVariable("instanceID") String instanceID) {
        ProcessingElement processingElement = peInstanceRepository.getInstance(instanceID);
        if (processingElement != null) {
            boolean terminated = processingElement.terminate();
            if (terminated) {
                peInstanceRepository.removeInstance(instanceID);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().body(null);
    }
}
