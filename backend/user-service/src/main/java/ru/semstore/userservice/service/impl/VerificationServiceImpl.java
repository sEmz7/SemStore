package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.common.dto.VerificationCodeEvent;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.VerificationCode;
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
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducer kafka;

    @Override
    public void createVerificationCode(User user) {
        codeRepository.deleteByUserId(user.getId());

        String code = generateCode();

        VerificationCode entity = new VerificationCode();
        entity.setUser(user);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        codeRepository.save(entity);
        kafka.sendVerificationCode(new VerificationCodeEvent(user.getEmail(), code));
    }

    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }
}
