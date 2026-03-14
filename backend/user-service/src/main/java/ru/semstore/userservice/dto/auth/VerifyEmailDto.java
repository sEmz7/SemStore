package ru.semstore.userservice.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailDto(
        @NotNull
        @Email
        String email,

        @NotNull
        String code
) {
}
