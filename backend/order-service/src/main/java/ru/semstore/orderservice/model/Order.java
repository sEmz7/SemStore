package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность заказа.
 *
 * <p>Хранит основную информацию о заказе:
 * владельца, адрес доставки, статус, дату создания, трек номер.</p>
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
     * Название заказа.
     */
    @Column(name = "name", nullable = false)
    private String name;

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

    /**
     * Товары заказа.
     */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Итоговая цена заказа.
     */
    @Column(name = "total_price", precision = 9, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Трек номер заказа.
     */
    @EqualsAndHashCode.Include
    @Column(name = "tracking_number", nullable = false, unique = true, length = 20)
    private String trackingNumber;
}
