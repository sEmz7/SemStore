package ru.semstore.userservice.service;

import java.util.UUID;

/**
 * Сервис для обновления кол-ва попыток верификации пользователя.
 */
public interface VerificationCodeAttemptsService {

    /**
     * Увеличивает количество попыток ввода кода подтверждения.
     *
     * @param codeId идентификатор кода подтверждения
     */
    void incrementAttempts(UUID codeId);
}
