package ru.semstore.userservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.VerificationCodeEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ORDER_CHECKED_TOPIC = "order-checked";
    private final String EMAIL_VERIFICATION_TOPIC = "email-verification-requested";

    public void sendCheckedOrder(OrderCheckedEvent checkedOrder) {
        kafkaTemplate.send(ORDER_CHECKED_TOPIC, checkedOrder);
        log.debug("OrderChecked sent to kafka, orderId={}", checkedOrder.getOrderId());
    }

    public void sendVerificationCode(VerificationCodeEvent verificationCodeEvent) {
        kafkaTemplate.send(EMAIL_VERIFICATION_TOPIC, verificationCodeEvent.email(), verificationCodeEvent);
        log.debug("Verification code sent to kafka");
    }
}
