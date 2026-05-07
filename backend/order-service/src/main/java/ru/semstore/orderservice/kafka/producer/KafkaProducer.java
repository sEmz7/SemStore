package ru.semstore.orderservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCompletedEvent;
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
    private static final String ORDER_CREATED_TOPIC = "order-created";

    /** Топик для событий завершения заказа */
    private static final String ORDER_COMPLETED_TOPIC = "q-completed-analytics";

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

    /**
     * Отправляет событие завершения заказа в analytics service.
     *
     * <p>Формирует событие {@link OrderCompletedEvent} и публикует его в Kafka
     * для последующей обработки analytics-service.</p>
     *
     * @param order завершённый заказ
     */
    public void sendOrderCompleted(Order order) {
        OrderCompletedEvent event = new OrderCompletedEvent(
                order.getId(), order.getUserId(), order.getTotalPrice());
        kafkaTemplate.send(ORDER_COMPLETED_TOPIC, event);
        log.debug("Order completed event sent to kafka, orderId={}", order.getId());
        System.out.println("отправлен в аналитику после завершения заказа, orderId=" + event.getOrderId());
    }
}
