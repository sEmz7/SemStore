package ru.semstore.userservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.userservice.dto.auth.ResendVerificationCodeDto;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.VerificationCode;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.repository.VerificationCodeRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class VerificationServiceIT {
    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository codeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private KafkaProducer kafka;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final String email = "test@mail.com";

    @Test
    @DisplayName("Создание кода верификации")
    @Sql({"/data/verification/cleanUp.sql", "/data/verification/insertUser.sql"})
    void createVerificationCode_ShouldSaveCode() {
        User user = userRepository.findByEmail(email).orElseThrow();

        verificationService.createVerificationCode(user);

        Optional<VerificationCode> savedCode = codeRepository.findVerificationCodeByUserId(user.getId());

        assertTrue(savedCode.isPresent());
        assertNotNull(savedCode.get().getCodeHash());
        assertNotNull(savedCode.get().getCreatedAt());
        assertNotNull(savedCode.get().getExpiresAt());
        assertEquals(user.getId(), savedCode.get().getUser().getId());

        verify(kafka, times(1)).sendVerificationCode(any(VerificationCodeEvent.class));
    }

    @Test
    @DisplayName("Создание кода верификации должно удалять старый код")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUserWithCode.sql")
    void createVerificationCode_ShouldReplaceOldCode() {
        User user = userRepository.findByEmail(email).orElseThrow();

        verificationService.createVerificationCode(user);

        var allCodes = codeRepository.findAll();

        assertEquals(1, allCodes.size());
        assertEquals(user.getId(), allCodes.getFirst().getUser().getId());

        verify(kafka, times(1)).sendVerificationCode(any(VerificationCodeEvent.class));
    }

    @Test
    @DisplayName("Подтверждение почты успешное")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUser.sql")
    void verifyEmail_ShouldConfirmEmail() {
        User user = userRepository.findByEmail(email).orElseThrow();

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCodeHash(passwordEncoder.encode("123456"));
        code.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        code.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        code.setAttempts(0);
        codeRepository.save(code);

        verificationService.verifyEmail(new VerifyEmailDto(email, "123456"));

        User updatedUser = userRepository.findByEmail(email).orElseThrow();
        Optional<VerificationCode> deletedCode = codeRepository.findVerificationCodeByUserId(user.getId());

        assertTrue(updatedUser.isEmailVerified());
        assertTrue(deletedCode.isEmpty());
    }

    @Test
    @DisplayName("Подтверждение почты с неверным кодом должно увеличить attempts")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUser.sql")
    void verifyEmail_ShouldIncrementAttempts_WhenCodeInvalid() {
        User user = userRepository.findByEmail(email).orElseThrow();

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCodeHash(passwordEncoder.encode("123456"));
        code.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        code.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        code.setAttempts(0);
        code = codeRepository.save(code);

        assertThrows(ConflictException.class,
                () -> verificationService.verifyEmail(new VerifyEmailDto(email, "654321")));

        VerificationCode updatedCode = codeRepository.findById(code.getId()).orElseThrow();
        User unchangedUser = userRepository.findByEmail(email).orElseThrow();

        assertEquals(1, updatedCode.getAttempts());
        assertFalse(unchangedUser.isEmailVerified());
    }

    @Test
    @DisplayName("Подтверждение почты просроченным кодом должно выбрасывать исключение")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUser.sql")
    void verifyEmail_ShouldThrow_WhenCodeExpired() {
        User user = userRepository.findByEmail(email).orElseThrow();

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCodeHash(passwordEncoder.encode("123456"));
        code.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        code.setAttempts(0);
        codeRepository.save(code);

        assertThrows(ConflictException.class,
                () -> verificationService.verifyEmail(new VerifyEmailDto(email, "123456")));

        User unchangedUser = userRepository.findByEmail(email).orElseThrow();
        assertFalse(unchangedUser.isEmailVerified());
    }

    @Test
    @DisplayName("Переотправка кода успешная")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUser.sql")
    void resendVerificationCode_ShouldResend() {
        User user = userRepository.findByEmail(email).orElseThrow();

        VerificationCode oldCode = new VerificationCode();
        oldCode.setUser(user);
        oldCode.setCodeHash(passwordEncoder.encode("111111"));
        oldCode.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        oldCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        oldCode.setAttempts(0);
        codeRepository.save(oldCode);

        verificationService.resendVerificationCode(new ResendVerificationCodeDto(email));

        var allCodes = codeRepository.findAll();
        assertEquals(1, allCodes.size());
        assertEquals(user.getId(), allCodes.getFirst().getUser().getId());

        verify(kafka, times(1)).sendVerificationCode(any(VerificationCodeEvent.class));
    }

    @Test
    @DisplayName("Переотправка кода раньше минуты должна выбрасывать исключение")
    @Sql("/data/verification/cleanUp.sql")
    @Sql("/data/verification/insertUser.sql")
    void resendVerificationCode_ShouldThrow_WhenTooEarly() {
        User user = userRepository.findByEmail(email).orElseThrow();

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCodeHash(passwordEncoder.encode("111111"));
        code.setCreatedAt(LocalDateTime.now().minusSeconds(20));
        code.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        code.setAttempts(0);
        codeRepository.save(code);

        assertThrows(ConflictException.class,
                () -> verificationService.resendVerificationCode(new ResendVerificationCodeDto(email)));

        var allCodes = codeRepository.findAll();
        assertEquals(1, allCodes.size());

        verify(kafka, never()).sendVerificationCode(any());
    }

    @Test
    @DisplayName("Подтверждение почты должно выбрасывать исключение если пользователь не найден")
    @Sql("/data/verification/cleanUp.sql")
    void verifyEmail_ShouldThrow_WhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> verificationService.verifyEmail(new VerifyEmailDto("unknown@mail.com", "123456")));
    }
}
