package ru.semstore.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность адреса пользователя.
 */
@Entity
@Table(name = "user_address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    /**
     * Уникальный идентификатор адреса.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    /**
     * Пользователь, к которому относится адрес.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Имя получателя.
     */
    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;

    /**
     * Фамилия получателя.
     */
    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;

    /**
     * Отчество получателя.
     */
    @Column(name = "patronymic", nullable = false, length = 100)
    private String patronymic;

    /**
     * Контактный телефон.
     */
    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    /**
     * Город.
     */
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    /**
     * Улица.
     */
    @Column(name = "street", nullable = false, length = 100)
    private String street;

    /**
     * Номер дома.
     */
    @Column(name = "building", nullable = false, length = 100)
    private String building;

    /**
     * Почтовый индекс.
     */
    @Column(name = "postal_code", nullable = false, length = 100)
    private String postalCode;
}