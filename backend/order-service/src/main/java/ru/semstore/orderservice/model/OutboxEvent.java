package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность outbox события.
 *
 * <p>Хранит основную информацию о событии:
 * тип, тело, статус, дату создания и кол-во попыток.</p>
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OutboxEvent {

    /**
     * ID события.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    /**
     * Тип события.
     */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /**
     * Тело события.
     */
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    /**
     * Статус события.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    /**
     * Дата создания события.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Количество попыток отправки события.
     */
    @Column(name = "attempts", nullable = false)
    private int attempts;

    /**
     * Метод для создания нового события.
     */
    public static OutboxEvent newEvent(String eventType, String payload) {
        OutboxEvent e = new OutboxEvent();
        e.setEventType(eventType);
        e.setPayload(payload);
        e.setStatus(OutboxEventStatus.NEW);
        e.setAttempts(0);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }
}
