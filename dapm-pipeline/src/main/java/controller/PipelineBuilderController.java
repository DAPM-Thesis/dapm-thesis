package controller;

import communication.API.request.PEInstanceRequest;
import communication.API.response.PEInstanceResponse;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import pipeline.processingelement.operator.Operator;
import pipeline.processingelement.source.Source;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.port}")
    private String brokerURL;

    private final TemplateRepository templateRepository;
    private final PEInstanceRepository peInstanceRepository;

    @Autowired
    public PipelineBuilderController(TemplateRepository templateRepository, PEInstanceRepository peInstanceRepository) {
        this.templateRepository = templateRepository;
        this.peInstanceRepository = peInstanceRepository;
    }

    @PostMapping("/source/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> configureSource(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Source<Message> source = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (source != null) {
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            source.registerProducer(producerConfig);
            String instanceID = peInstanceRepository.storeInstance(source);

            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/operator/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createOperator(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Operator<Message, Message> operator = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (operator != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                operator.registerConsumer(config);
            }
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            operator.registerProducer(producerConfig);

            String instanceID = peInstanceRepository.storeInstance(operator);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/sink/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createSink(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Sink sink = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (sink != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                sink.registerConsumer(config);
            }
            String instanceID = peInstanceRepository.storeInstance(sink);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/start/instance/{instanceID}")
    public ResponseEntity<Void> startSource(@PathVariable("instanceID") String instanceID) {
        Source<Message> source = peInstanceRepository.getInstance(instanceID);
        if (source != null) {
            source.start();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }
}
