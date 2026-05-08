package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.orderservice.model.UserDiscount;

import java.util.Optional;
import java.util.UUID;

public interface UserDiscountRepository extends JpaRepository<UserDiscount, UUID> {

    Optional<UserDiscount> findByUserId(UUID userId);
}
