import candidate_validation.CandidateParserTest;
import candidate_validation.PipelineCandidate;
import candidate_validation.ValidatedPipeline;
import communication.API.HTTPClient;
import communication.API.HTTPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import testconfig.PipelineBuilderTestConfig;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PipelineBuilderTestConfig.class})
public class PipelineBuilderTest {
    @Autowired
    private PipelineBuilder pipelineBuilder;
    @Autowired
    private HTTPClient httpClient;

    // has to be a valid pipeline candidate to build a pipeline.
    private final String pipelineCandidatePath = "src/test/resources/candidate_validation/simple_valid.json";

    private final String templateIDSource = "$$$ Source";
    private final String templateIDOperator = "The Profit Miner";
    private final String templateIDSink = "Dream Sink";
    private final String instanceIDSource = "source-id";
    private final String instanceIDOperator = "operator-id";
    private final String instanceIDSink = "sink-id";
    private final String brokerURL = "url";
    private final String topic = "topic";

    @BeforeEach
    public void setUp() {
        Mockito.reset(httpClient);
    }

    private String responseWithProducerConfig(String templateID, String instanceID, String brokerURL, String topic, boolean producerConfigExists) {
        StringBuilder json = new StringBuilder("{");
        boolean needsComma = false;
        if (templateID != null) {
            json.append("\"templateID\":\"").append(templateID).append("\"");
            needsComma = true;
        }
        if (instanceID != null) {
            if (needsComma) json.append(",");
            json.append("\"instanceID\":\"").append(instanceID).append("\"");
            needsComma = true;
        }
        if (producerConfigExists) {
            if (needsComma) json.append(",");
            json.append("\"producerConfig\":{\"brokerURL\":\"").append(brokerURL)
                    .append("\",\"topic\":\"").append(topic).append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String responseWithoutProducerConfig(String templateID, String instanceID) {
        StringBuilder json = new StringBuilder("{");
        boolean needsComma = false;
        if (templateID != null) {
            json.append("\"templateID\":\"").append(templateID).append("\"");
            needsComma = true;
        }
        if (instanceID != null) {
            if (needsComma) json.append(",");
            json.append("\"instanceID\":\"").append(instanceID).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private void setUpMockResponses(Function<String, String> bodyForUrl) {
        Mockito.when(httpClient.postSync(Mockito.anyString(), Mockito.any()))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    HTTPResponse mockResponse = Mockito.mock(HTTPResponse.class);
                    Mockito.when(mockResponse.body()).thenReturn(bodyForUrl.apply(url));
                    return mockResponse;
                });

        Mockito.when(httpClient.postSync(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    HTTPResponse mockResponse = Mockito.mock(HTTPResponse.class);
                    Mockito.when(mockResponse.body()).thenReturn(bodyForUrl.apply(url));
                    return mockResponse;
                });
    }

    private void setUpSuccessfulMockResponses() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(templateIDSource, instanceIDSource, brokerURL, topic, true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(templateIDOperator, instanceIDOperator, brokerURL, topic, true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(templateIDSink, instanceIDSink);
            }
            return null;
        });
    }

    private void setUpFailedMockResponsesNullBody() {
        setUpMockResponses(url -> null);
    }

    private void setUpFailedMockResponsesNullTemplateId() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(null, instanceIDSource, brokerURL, topic, true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(null, instanceIDOperator, brokerURL, topic, true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(null, instanceIDSink);
            }
            return null;
        });
    }

    private void setUpFailedMockResponsesNullProducerConfigs() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(templateIDSource, instanceIDSource, brokerURL, topic, false);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(templateIDOperator, instanceIDOperator, brokerURL, topic, false);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(templateIDSink, instanceIDSink);
            }
            return null;
        });
    }

    void setUpFailedMockResponsesNullInstanceID() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(templateIDSource, null, brokerURL, topic, true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(templateIDOperator, null, brokerURL, topic, true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(templateIDSink, null);
            }
            return null;
        });
    }

    void setUpFailedMockResponsesEmptyTemplateID() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig("", instanceIDSource, brokerURL, topic, true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig("", instanceIDOperator, brokerURL, topic, true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig("", instanceIDSink);
            }
            return null;
        });
    }

    void setUpFailedMockResponsesEmptyInstanceID() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(templateIDSource, "", brokerURL, topic, true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(templateIDOperator, "", brokerURL, topic, true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(templateIDSink, "");
            }
            return null;
        });
    }

    void setUpFailedMockResponsesEmptyProducerConfigs() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return responseWithProducerConfig(templateIDSource, instanceIDSource, "", "", true);
            } else if (url.contains("/operator")) {
                return responseWithProducerConfig(templateIDOperator, instanceIDOperator, "", "", true);
            } else if (url.contains("/sink")) {
                return responseWithoutProducerConfig(templateIDSink, instanceIDSink);
            }
            return null;
        });
    }

    void setUpFailedMockResponsesNullResponse() {
        Mockito.when(httpClient.postSync(Mockito.anyString(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(httpClient.postSync(Mockito.anyString()))
                .thenReturn(null);
    }

    private void stubTokenFetch() {
        Mockito.when(httpClient.getSync(Mockito.contains("/token?instanceId=")))
               .thenAnswer(inv -> {
                   HTTPResponse tokenResp = Mockito.mock(HTTPResponse.class);
                   Mockito.when(tokenResp.status())
                          .thenReturn(org.springframework.http.HttpStatus.OK);
                   Mockito.when(tokenResp.body())
                          .thenReturn("dummy-jwt");
                   return tokenResp;
               });
    }

    @Test
    public void success() {
        setUpSuccessfulMockResponses();
        stubTokenFetch();
        String orgID = "org1";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        Pipeline pipeline = pipelineBuilder.buildPipeline(orgID, validatedPipeline);

        assertNotNull(pipeline);
        assertEquals(orgID, pipeline.getOrganizationOwnerID());
        assertEquals(3, pipeline.getProcessingElements().size());
        assertNotNull(pipeline.getProcessingElements().get(instanceIDSource));
        assertNotNull(pipeline.getProcessingElements().get(instanceIDOperator));
        assertNotNull(pipeline.getProcessingElements().get(instanceIDSink));
    }

    @Test
    public void fail_null_body() {
        setUpFailedMockResponsesNullBody();
        String orgID = "org1";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_template_id() {
        setUpFailedMockResponsesNullTemplateId();
        String orgID = "org1";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);
        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_producer_configs() {
        setUpFailedMockResponsesNullProducerConfigs();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_instance_id() {
        setUpFailedMockResponsesNullInstanceID();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_empty_template_id() {
        setUpFailedMockResponsesEmptyTemplateID();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_empty_instance_id() {
        setUpFailedMockResponsesEmptyInstanceID();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_empty_producer_configs() {
        setUpFailedMockResponsesEmptyProducerConfigs();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_response() {
        setUpFailedMockResponsesNullResponse();
        String orgID = "org1";

        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(pipelineCandidatePath);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(candidate);

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(orgID, validatedPipeline)
        );
    }
}