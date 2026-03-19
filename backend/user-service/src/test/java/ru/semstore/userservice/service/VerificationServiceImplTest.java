package ru.semstore.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.userservice.dto.auth.ResendVerificationCodeDto;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.VerificationCode;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.repository.VerificationCodeRepository;
import ru.semstore.userservice.service.impl.VerificationCodeAttemptsServiceImpl;
import ru.semstore.userservice.service.impl.VerificationServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class VerificationServiceImplTest {

    @Mock
    VerificationCodeRepository codeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    KafkaProducer kafka;
    @Mock
    VerificationCodeAttemptsServiceImpl verificationCodeAttemptsService;

    @InjectMocks
    VerificationServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createVerificationCode_shouldGenerateAndSendCode() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@mail.com");

        service.createVerificationCode(user);

        verify(codeRepository).deleteByUserId(user.getId());
        verify(codeRepository).save(any(VerificationCode.class));
        verify(kafka).sendVerificationCode(any(VerificationCodeEvent.class));
    }

    @Test
    void createVerificationCode_shouldEncodeCode() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@mail.com");

        service.createVerificationCode(user);

        verify(passwordEncoder, atLeastOnce()).encode(anyString());
    }

    @Test
    void verifyEmail_shouldVerifyUser_whenCodeMatches() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmail("a@mail.com");
        user.setEmailVerified(false);

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCodeHash("hashed");
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setAttempts(0);

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(codeRepository.findVerificationCodeByUserId(id)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);

        service.verifyEmail(new VerifyEmailDto("a@mail.com", "123456"));

        assertTrue(user.isEmailVerified());
        verify(codeRepository).delete(code);
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_shouldThrow_whenCodeExpired() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmailVerified(false);
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(codeRepository.findVerificationCodeByUserId(id)).thenReturn(Optional.of(code));

        assertThrows(ConflictException.class, () ->
                service.verifyEmail(new VerifyEmailDto("a@mail.com", "123456")));
    }

    @Test
    void verifyEmail_shouldThrow_whenInvalidCode() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmailVerified(false);
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setCodeHash("hashed");
        code.setAttempts(0);

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(codeRepository.findVerificationCodeByUserId(id)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(ConflictException.class, () ->
                service.verifyEmail(new VerifyEmailDto("a@mail.com", "wrong")));
    }

    @Test
    void resendVerificationCode_shouldCallCreateVerificationCode() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmailVerified(false);
        VerificationCode oldCode = new VerificationCode();
        oldCode.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(codeRepository.findVerificationCodeByUserId(id)).thenReturn(Optional.of(oldCode));

        VerificationServiceImpl spyService = spy(service);
        doNothing().when(spyService).createVerificationCode(user);

        spyService.resendVerificationCode(new ResendVerificationCodeDto("a@mail.com"));

        verify(spyService).createVerificationCode(user);
    }

    @Test
    void resendVerificationCode_shouldThrowIfTooSoon() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmailVerified(false);
        VerificationCode oldCode = new VerificationCode();
        oldCode.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(codeRepository.findVerificationCodeByUserId(id)).thenReturn(Optional.of(oldCode));

        assertThrows(ConflictException.class, () ->
                service.resendVerificationCode(new ResendVerificationCodeDto("a@mail.com")));
    }

    @Test
    void resendVerificationCode_shouldThrowIfAlreadyVerified() {
        User user = new User();
        user.setEmailVerified(true);

        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () ->
                service.resendVerificationCode(new ResendVerificationCodeDto("a@mail.com")));
    }
}
