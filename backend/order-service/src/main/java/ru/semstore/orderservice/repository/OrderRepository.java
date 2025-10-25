package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.orderservice.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
