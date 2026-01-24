package ru.semstore.orderservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;

import java.util.Optional;

/**
 * Kafka-консьюмер для обработки событий, связанных с заказами.
 *
 * <p>Слушает события проверки заказа, поступающие из user-service,
 * и обновляет статус заказа в зависимости от результата проверки.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;

    /**
     * Обрабатывает событие проверки заказа.
     *
     * <p>Получает событие {@link OrderCheckedEvent} и обновляет статус заказа:
     * <ul>
     *     <li>{@link OrderStatus#CREATED} — если заказ прошёл проверку</li>
     *     <li>{@link OrderStatus#CANCELED} — если проверка не пройдена</li>
     * </ul>
     *
     * <p>Если заказ с указанным идентификатором не найден, событие игнорируется.</p>
     *
     * @param checkedOrder событие с результатом проверки заказа
     */
    @KafkaListener(topics = "order-checked")
    public void orderCheckedListen(OrderCheckedEvent checkedOrder) {
        log.debug("Received checked order, orderId={}", checkedOrder.getOrderId());
        Optional<Order> orderOptional = orderRepository.findById(checkedOrder.getOrderId());
        if (orderOptional.isEmpty()) {
            log.warn("Order not found with id={}", checkedOrder.getOrderId());
            return;
        }
        Order order = orderOptional.get();
        order.setStatus(checkedOrder.getValid() ? OrderStatus.CREATED : OrderStatus.CANCELED);
        log.debug("Order status changed to={}, orderId{}", order.getStatus(), order.getId());
        orderRepository.save(order);
    }
}
