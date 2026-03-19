package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.userservice.dto.auth.ResendVerificationCodeDto;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.ErrorCode;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.VerificationCode;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.repository.VerificationCodeRepository;
import ru.semstore.userservice.service.VerificationCodeAttemptsService;
import ru.semstore.userservice.service.VerificationService;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Реализация сервиса верификации пользователя.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final VerificationCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducer kafka;
    private final VerificationCodeAttemptsService verificationCodeAttemptsService;
    private final int VERIFICATION_CODE_LIFETIME = 15;
    private final int MAX_ATTEMPTS_TO_VERIFY_EMAIL = 5;

    /**
     * Генерирует новый код подтверждения, сохраняет его и отправляет пользователю.
     *
     * <p>Предыдущий код удаляется перед созданием нового.</p>
     *
     * @param user пользователь, для которого создается код
     */
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

    /**
     * Подтверждает email пользователя по введенному коду.
     *
     * <p>При успешной проверке помечает email как подтвержденный
     * и удаляет код.</p>
     *
     * @param dto данные с email и кодом подтверждения
     */
    @Override
    public void verifyEmail(VerifyEmailDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        if (user.isEmailVerified()) {
            throw new ConflictException("User already verified", ErrorCode.USER_ALREADY_VERIFIED);
        }
        VerificationCode code = codeRepository.findVerificationCodeByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Code not found", ErrorCode.CODE_NOT_FOUND));
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Verification code expired", ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (code.getAttempts() >= MAX_ATTEMPTS_TO_VERIFY_EMAIL) {
            throw new ConflictException("Too many attempts", ErrorCode.TO_MANY_ATTEMPTS_TO_VERIFY_EMAIL);
        }
        if (!passwordEncoder.matches(dto.code(), code.getCodeHash())) {
            verificationCodeAttemptsService.incrementAttempts(code.getId());
            throw new ConflictException("Invalid verification code", ErrorCode.INVALID_VERIFICATION_CODE);
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        codeRepository.delete(code);
    }

    /**
     * Повторно отправляет код подтверждения email пользователю.
     *
     * <p>При успешной проверке генерирует и отправляет новый код.</p>
     *
     * @param dto данные с email пользователя
     */
    @Override
    public void resendVerificationCode(ResendVerificationCodeDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        if (user.isEmailVerified()) {
            throw new ConflictException("User already verified", ErrorCode.USER_ALREADY_VERIFIED);
        }
        VerificationCode code = codeRepository.findVerificationCodeByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Code not found", ErrorCode.CODE_NOT_FOUND));
        if (code.getCreatedAt().plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new ConflictException("Wait 1 minute to resend the code", ErrorCode.WAIT_FOR_RESEND_CODE);
        }
        createVerificationCode(user);
    }

    /**
     * Генерирует случайный шестизначный код.
     */
    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }
}
