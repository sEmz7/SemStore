package ru.semstore.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.semstore.userservice.dto.eventDto.OrderCreatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${SPRING_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrap_servers;

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "semstore-group");

        JsonDeserializer<OrderCreatedEvent> jsonDeserializer =
                new JsonDeserializer<>(OrderCreatedEvent.class, objectMapper);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> consumerFactory
    ) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>();
        containerFactory.setConcurrency(1);
        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory;
    }
}
