package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "address_id")
    private UUID addressId;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
