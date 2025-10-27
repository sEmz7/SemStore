package ru.semstore.userservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.userservice.dto.eventDto.OrderCreatedEvent;

@Service
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = "order-created", groupId = "consumer")
    public void listen(OrderCreatedEvent event) {
        log.warn("ПОЛУЧЕНО: {}", event.toString());
    }
}
