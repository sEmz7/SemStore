package ru.semstore.userservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ORDER_CHECKED_TOPIC = "order-checked";

    public void sendCheckedOrder(OrderCheckedEvent checkedOrder) {
        kafkaTemplate.send(ORDER_CHECKED_TOPIC, checkedOrder);
        log.debug("OrderChecked sent to kafka, orderId={}", checkedOrder.getOrderId());
    }
}
