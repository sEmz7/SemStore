package ru.semstore.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.userservice.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
