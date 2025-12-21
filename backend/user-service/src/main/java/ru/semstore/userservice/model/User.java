package ru.semstore.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность пользователя.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /**
     * Email пользователя.
     */
    @EqualsAndHashCode.Include
    @Column(name = "email", unique = true, nullable = false, updatable = false)
    private String email;

    /**
     * Хэш пароля пользователя.
     */
    @Column(name = "password", nullable = false)
    private String password;
}
