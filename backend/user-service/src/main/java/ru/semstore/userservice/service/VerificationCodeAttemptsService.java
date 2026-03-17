package ru.semstore.userservice.service;

import java.util.UUID;

public interface VerificationCodeAttemptsService {

    void incrementAttempts(UUID codeId);
}
