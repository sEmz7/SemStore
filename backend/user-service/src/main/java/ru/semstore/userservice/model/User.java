package ru.semstore.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @EqualsAndHashCode.Include
    @Column(name = "email", unique = true, nullable = false, updatable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;
}
