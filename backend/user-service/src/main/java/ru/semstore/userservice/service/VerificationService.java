package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.auth.ResendVerificationCodeDto;
import ru.semstore.userservice.dto.auth.VerifyEmailDto;
import ru.semstore.userservice.model.User;

public interface VerificationService {

    void createVerificationCode(User user);

    void verifyEmail(VerifyEmailDto dto);

    void resendVerificationCode(ResendVerificationCodeDto dto);
}
