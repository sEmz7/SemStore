package ru.semstore.userservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic emailVerifyConfig() {
        return TopicBuilder.name("email-verification-requested")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
