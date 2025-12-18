package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность товара в заказе.
 *
 * <p>Описывает отдельный товар, входящий в состав заказа.</p>
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItem {

    /**
     * Уникальный идентификатор позиции заказа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    /**
     * Заказ, к которому относится товар.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Ссылка на товар.
     */
    @Column(name = "link", nullable = false, length = 50)
    private String link;

    /**
     * Размер товара.
     */
    @Column(name = "size", nullable = false, length = 30)
    private String size;

    /**
     * Конфигурация товара.
     */
    @Column(name = "configuration", nullable = false)
    private String configuration;
}
