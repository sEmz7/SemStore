package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.orderservice.model.OutboxEventStatus;
import ru.semstore.orderservice.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findAllByStatus(OutboxEventStatus status);
}