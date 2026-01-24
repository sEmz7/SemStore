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

/**
 * Сервис для обработки outbox-событий и отправки их в Kafka.
 *
 * <p>Реализует паттерн Outbox для гарантированной доставки событий
 * при асинхронном взаимодействии между сервисами.</p>
 *
 * <p>Сервис периодически выбирает новые события из базы данных,
 * отправляет их в Kafka и обновляет их статус в зависимости
 * от результата отправки.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Обрабатывает outbox-события со статусом {@link OutboxEventStatus#NEW}.
     *
     * <p>Метод выполняется каждые 10 минут, отправляет события в Kafka
     * и помечает их как {@link OutboxEventStatus#SENT} в случае успеха.
     * При ошибке увеличивает счётчик попыток и переводит событие
     * в статус {@link OutboxEventStatus#FAILED} при превышении лимита.</p>
     */
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
