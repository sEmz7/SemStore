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

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;

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
