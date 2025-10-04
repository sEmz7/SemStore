package ru.semstore.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "user_address")
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private String firstname;

    private String lastname;

    private String patronymic;

    private String phone;

    private String city;

    private String street;

    private String building;

    @Column(name = "postal_code")
    private Integer postalCode;
}
