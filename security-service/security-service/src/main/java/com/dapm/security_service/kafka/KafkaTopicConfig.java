package com.dapm.security_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic pipelineRequestTopic() {
        return new NewTopic("pipeline-node-requests", 3, (short) 1);
    }

    @Bean
    public NewTopic pipelineApprovalTopic() {
        return new NewTopic("pipeline-node-approvals", 3, (short) 1);
    }
}
