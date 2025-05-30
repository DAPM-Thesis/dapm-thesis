package controller;

import communication.API.request.PEInstanceRequest;
import communication.API.response.PEInstanceResponse;
import communication.ConsumerFactory;
import communication.ProducerFactory;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pipeline.Pipeline;
import pipeline.notification.PipelineNotificationService;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.Sink;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.HeartbeatTopicConfig;
import pipeline.processingelement.operator.Operator;
import pipeline.processingelement.source.Source;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;
import utils.LogUtil;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.port}")
    private String brokerURL;

    private final ProducerFactory producerFactory;
    private final ConsumerFactory consumerFactory;
    private final TemplateRepository templateRepository;
    private final PEInstanceRepository peInstanceRepository;

    @Autowired
    private PipelineNotificationService notificationService;

    @Autowired
    public PipelineBuilderController(TemplateRepository templateRepository,
                                     PEInstanceRepository peInstanceRepository,
                                     ConsumerFactory consumerFactory,
                                     ProducerFactory producerFactory) {
        this.templateRepository = templateRepository;
        this.peInstanceRepository = peInstanceRepository;
        this.consumerFactory = consumerFactory;
        this.producerFactory = producerFactory;
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
            producerFactory.registerProducer(source, producerConfig);
            String instanceID = peInstanceRepository.storeInstance(source);
            source.setInstanceId(instanceID);

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
                consumerFactory.registerConsumer(operator, config);
            }
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            producerFactory.registerProducer(operator, producerConfig);

            String instanceID = peInstanceRepository.storeInstance(operator);
            operator.setInstanceId(instanceID);

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
                consumerFactory.registerConsumer(sink, config);
            }
            String instanceID = peInstanceRepository.storeInstance(sink);
            sink.setInstanceId(instanceID);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/heartbeat/instance/{instanceID}")
    public ResponseEntity<Void> configureHeartbeat(
            @PathVariable("instanceID") String instanceID,
            @RequestBody HeartbeatTopicConfig heartbeatTopicConfig) { 

        ProcessingElement processingElement = peInstanceRepository.getInstance(instanceID);
        if (processingElement == null) {
            LogUtil.info("[CONTROLLER HB] PE instance {} not found for heartbeat config.", instanceID);
            return ResponseEntity.notFound().build();
        }
        processingElement.configureHeartbeatTopics(heartbeatTopicConfig);
        LogUtil.info("[CONTROLLER HB] Configured HeartbeatTopicConfig for PE {}", instanceID);
        return ResponseEntity.ok().build();
    }

    // TODO: Move to somewhere else
    public static record OperationalParamsRequest(String pipelineId, FaultToleranceLevel faultToleranceLevel, String organizationHostURL) {}

    @PutMapping("/instance/{instanceID}/operational-params")
    public ResponseEntity<Void> setOperationalParams(
            @PathVariable("instanceID") String instanceID,
            @RequestBody OperationalParamsRequest params) {
        ProcessingElement pe = peInstanceRepository.getInstance(instanceID);
        if (pe == null) { return ResponseEntity.notFound().build(); }
        
        pe.setOperationalParameters(params.pipelineId(), params.faultToleranceLevel(), notificationService, params.organizationHostURL);
        LogUtil.info("[CTRLR OP PARAMS] Set operational params for PE {}: PipelineID={}, FTLevel={}",
                instanceID, params.pipelineId(), params.faultToleranceLevel());
        return ResponseEntity.ok().build();
    }
}