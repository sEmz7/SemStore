package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность заказа.
 *
 * <p>Хранит основную информацию о заказе:
 * владельца, адрес доставки, статус и дату создания.</p>
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    /**
     * Уникальный идентификатор заказа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /**
     * Идентификатор владельца заказа.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Идентификатор адреса доставки.
     */
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    /**
     * Текущий статус заказа.
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    /**
     * Дата и время создания заказа.
     */
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
}
