package ru.semstore.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.notificationservice.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final EmailService emailService;

    @KafkaListener(topics = "order-checked")
    public void orderCheckedNotification() {

    }
}
