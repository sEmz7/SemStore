package ru.semstore.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Код для верификации почты.
 */
@Entity
@Table(name = "verification_code")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VerificationCode {

    /**
     * ID сущности.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Пользователь.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Хэщ кода.
     */
    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    /**
     * Дата окончания жизни кода.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Кол-во попыток верифицировать.
     */
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    /**
     * Дата создания кода.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}