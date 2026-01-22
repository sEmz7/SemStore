package ru.semstore.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.semstore.orderservice.model.OutboxEvent;
import ru.semstore.orderservice.model.OutboxEventStatus;
import ru.semstore.orderservice.repository.OutboxRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 600000)
    public void processOrderCompletedOutboxEvent() {
        List<OutboxEvent> outboxEvents = outboxRepository.findAllByStatus(OutboxEventStatus.NEW);

        for(OutboxEvent event: outboxEvents) {
            try {
                kafkaTemplate.send("order-completed", event.getPayload()).get();
                event.setStatus(OutboxEventStatus.SENT);
                outboxRepository.save(event);
            } catch (Exception e) {
                event.setAttempts(event.getAttempts() + 1);
                if (event.getAttempts() > 5) {
                    event.setStatus(OutboxEventStatus.FAILED);
                }
                outboxRepository.save(event);
            }
        }
    }
}
