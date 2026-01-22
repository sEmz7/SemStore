package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

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
