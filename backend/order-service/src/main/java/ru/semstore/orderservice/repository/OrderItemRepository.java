package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.semstore.orderservice.model.OrderItem;

import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT i FROM OrderItem i " +
            "LEFT JOIN FETCH i.order " +
            "WHERE i.id = :id")
    Optional<OrderItem> findItemByIdWithOrder(UUID id);
}
