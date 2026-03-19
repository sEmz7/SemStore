package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.auth.ResendVerificationCodeDto;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.model.User;

/**
 * Сервис для верификации пользователя
 */
public interface VerificationService {

    /**
     * Создает и отправляет код подтверждения для пользователя.
     *
     * @param user пользователь, для которого создается код
     */
    void createVerificationCode(User user);

    /**
     * Подтверждает email пользователя по коду.
     *
     * @param dto данные с email и кодом подтверждения
     */
    void verifyEmail(VerifyEmailDto dto);

    /**
     * Повторно отправляет код подтверждения email.
     *
     * @param dto данные с email пользователя
     */
    void resendVerificationCode(ResendVerificationCodeDto dto);
}
