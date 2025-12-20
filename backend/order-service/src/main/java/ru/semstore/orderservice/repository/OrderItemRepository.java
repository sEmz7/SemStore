package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.orderservice.model.OrderItem;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
