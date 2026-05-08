package ru.semstore.orderservice.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.semstore.common.dto.UserDiscountEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka-консьюмеров.
 *
 * <p>Создаёт специализированную фабрику контейнеров для топика {@code users-discounts},
 * который публикует Go-сервис аналитики без заголовка {@code __TypeId__}.
 * Для таких сообщений тип десериализации задаётся явно через
 * {@link JsonDeserializer#VALUE_DEFAULT_TYPE}.</p>
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Фабрика консьюмеров для топика {@code users-discounts}.
     *
     * <p>Отключает использование заголовка {@code __TypeId__} и явно указывает
     * целевой тип {@link UserDiscountEvent}, чтобы Jackson мог корректно
     * десериализовать JSON, отправляемый Go-сервисом.</p>
     */
    @Bean
    public ConsumerFactory<String, UserDiscountEvent> userDiscountConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserDiscountEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "ru.semstore.common.dto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Фабрика Kafka-контейнеров для топика {@code users-discounts}.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserDiscountEvent> userDiscountKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserDiscountEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userDiscountConsumerFactory());
        return factory;
    }
}
