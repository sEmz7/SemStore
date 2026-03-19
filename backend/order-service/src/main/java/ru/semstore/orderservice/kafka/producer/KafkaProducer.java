package ru.semstore.orderservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCreatedEvent;
import ru.semstore.orderservice.model.Order;

/**
 * Kafka-продюсер для отправки событий, связанных с заказами.
 *
 * <p>Используется для асинхронного взаимодействия с другими сервисами.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Топик для событий создания заказа */
    private final String ORDER_CREATED_TOPIC = "order-created";

    /**
     * Отправляет событие создания заказа на проверку.
     *
     * <p>Формирует событие {@link OrderCreatedEvent} и публикует его в Kafka
     * для последующей обработки user-service.</p>
     *
     * @param order заказ, отправляемый на проверку
     */
    public void sendOrderToCheck(Order order) {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(), order.getUserId(),
                order.getAddressId(), order.getTrackingNumber());

        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderCreatedEvent);
        log.debug("Order sent to kafka, orderId={}", order.getId());
    }
}
