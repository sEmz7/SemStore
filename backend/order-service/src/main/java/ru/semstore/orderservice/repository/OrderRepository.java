package ru.semstore.orderservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT r FROM Order r " +
            "WHERE (r.userId = :userId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (cast(:rangeStart as date) IS NULL OR r.createdDate >= :rangeStart) " +
            "AND (cast(:rangeEnd as date) IS NULL OR r.createdDate <= :rangeEnd)")
    Page<Order> findAllBySort(Pageable pageable,
                              @Param("userId") UUID userId,
                              @Param("status") OrderStatus status,
                              @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.id = :id")
    Optional<Order> findOrderByIdWithItems(UUID id);
}
