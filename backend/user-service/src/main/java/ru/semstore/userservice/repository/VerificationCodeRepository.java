package ru.semstore.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.userservice.model.VerificationCode;

import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    Optional<VerificationCode> findVerificationCodeByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
