package ru.semstore.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.notificationservice.service.EmailService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final EmailService emailService;

    @KafkaListener(topics = "order-checked")
    public void orderCheckedNotification(OrderCheckedEvent event) {
        log.debug("Received event, orderId={}", event.getOrderId());
        if (!event.getValid()) {
            log.debug("Order {} is invalid, skip email", event.getOrderId());
            return;
        }
        if (event.getUserEmail() == null || event.getUserEmail().isBlank()) {
            log.warn("Email is empty for order {}", event.getOrderId());
            return;
        }

        String subject = "Ваш заказ №" + event.getOrderId() + " создан";
        emailService.sendHtmlMessage(
                event.getUserEmail(),
                subject,
                "order-created",
                Map.of("orderId", event.getOrderId())
        );
    }

    @KafkaListener(topics = "email-verification-requested")
    public void emailVerificationCodeNotification(VerificationCodeEvent codeEvent) {
        log.debug("Received event");
        String subject = "Код подтверждения";
        emailService.sendHtmlMessage(
                codeEvent.email(),
                subject,
                "verification-code",
                Map.of("code", codeEvent.code())
        );
    }
}
