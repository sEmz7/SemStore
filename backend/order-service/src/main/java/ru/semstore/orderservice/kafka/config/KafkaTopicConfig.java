package ru.semstore.orderservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order-created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean NewTopic orderCheckedTopic() {
        return TopicBuilder.name("order-checked")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean NewTopic orderCompleteTopic() {
        return TopicBuilder.name("order-completed")
                .partitions(1)
                .replicas(1)
                .build();
    }
}

