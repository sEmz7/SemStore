package ru.semstore.userservice.service.impl;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.ErrorCode;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.VerificationCode;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.repository.VerificationCodeRepository;
import ru.semstore.userservice.service.VerificationService;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final VerificationCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducer kafka;
    private final int VERIFICATION_CODE_LIFETIME = 15;
    private final int MAX_ATTEMPTS_TO_VERIFY_EMAIL = 5;

    @Override
    public void createVerificationCode(User user) {
        codeRepository.deleteByUserId(user.getId());

        String code = generateCode();

        VerificationCode entity = new VerificationCode();
        entity.setUser(user);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_LIFETIME));

        codeRepository.save(entity);
        kafka.sendVerificationCode(new VerificationCodeEvent(user.getEmail(), code));
    }

    @Override
    public void verifyEmail(VerifyEmailDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.isEmailVerified()) {
            throw new ConflictException("User already verified", ErrorCode.USER_ALREADY_VERIFIED);
        }
        VerificationCode code = codeRepository.findVerificationCodeByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Code not found"));
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Verification code expired", ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (code.getAttempts() >= MAX_ATTEMPTS_TO_VERIFY_EMAIL) {
            throw new ConflictException("Too many attempts", ErrorCode.TO_MANY_ATTEMPTS_TO_VERIFY_EMAIL);
        }
        code.setAttempts(code.getAttempts() + 1);
        if (!passwordEncoder.matches(dto.code(), code.getCodeHash())) {
            codeRepository.save(code);
            throw new ConflictException("Invalid verification code", ErrorCode.INVALID_VERIFICATION_CODE);
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        codeRepository.delete(code);
    }

    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }
}
