package ru.semstore.orderservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCreatedEvent;
import ru.semstore.orderservice.model.Order;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ORDER_CREATED_TOPIC = "order-created";

    public void sendOrderToCheck(Order order) {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(), order.getUserId(),
                order.getAddressId());

        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderCreatedEvent);
        log.debug("Order sent to kafka, orderId={}", order.getId());
    }
}
