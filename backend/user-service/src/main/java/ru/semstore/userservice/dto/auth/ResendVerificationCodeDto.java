package ru.semstore.userservice.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResendVerificationCodeDto(
        @NotNull
        @Email
        String email
) {
}
