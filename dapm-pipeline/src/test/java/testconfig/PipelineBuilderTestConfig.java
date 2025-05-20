package testconfig;

import communication.API.HTTPClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pipeline.PipelineBuilder;

@TestConfiguration
public class PipelineBuilderTestConfig {
    @Bean
    public PipelineBuilder pipelineBuilder(HTTPClient httpClient) {
        return new PipelineBuilder(httpClient);
    }

    @Bean
    public HTTPClient httpClient() {
        return Mockito.mock(HTTPClient.class);
    }
}
