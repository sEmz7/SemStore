package ru.semstore.orderservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.UserDiscountEvent;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.model.UserDiscount;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.repository.UserDiscountRepository;

import java.util.Optional;

/**
 * Kafka-консьюмер для обработки событий, связанных с заказами и скидками пользователей.
 *
 * <p>Слушает события проверки заказа и события скидок пользователей.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;
    private final UserDiscountRepository userDiscountRepository;

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

    /**
     * Обрабатывает событие скидки пользователя.
     *
     * <p>Получает событие {@link UserDiscountEvent} и сохраняет (или обновляет)
     * скидку пользователя в базе данных.</p>
     *
     * @param event событие со скидкой пользователя
     */
    @Transactional
    @KafkaListener(topics = "users-discounts", containerFactory = "userDiscountKafkaListenerContainerFactory")
    public void userDiscountListen(UserDiscountEvent event) {
        log.debug("Received user discount event, userId={}", event.getUserId());
        System.out.println("получена скидка из аналитики, userId=" + event.getUserId() + " discount=" + event.getDiscountPercent());
        if (event.getDiscountPercent() == null || event.getDiscountPercent() < 0 || event.getDiscountPercent() > 100) {
            log.warn("Invalid discount percent in event, userId={}, percent={}", event.getUserId(), event.getDiscountPercent());
            return;
        }
        UserDiscount discount = userDiscountRepository.findByUserId(event.getUserId())
                .orElseGet(UserDiscount::new);
        discount.setUserId(event.getUserId());
        discount.setDiscountPercent(event.getDiscountPercent().intValue());
        userDiscountRepository.save(discount);
        log.debug("User discount saved, userId={}, percent={}", event.getUserId(), event.getDiscountPercent());
    }
}
