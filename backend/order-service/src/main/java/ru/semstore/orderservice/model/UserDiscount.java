package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность скидки пользователя.
 *
 * <p>Хранит информацию о персональной скидке пользователя,
 * полученной из analytics service.</p>
 */
@Entity
@Table(name = "users_discounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserDiscount {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    /**
     * Идентификатор пользователя.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Процент скидки пользователя (например, 10 = 10%).
     */
    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;
}
