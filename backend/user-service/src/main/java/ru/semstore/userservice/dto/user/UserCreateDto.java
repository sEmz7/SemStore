package ru.semstore.userservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные пользователя для регистрации")
public record UserCreateDto(
        @Schema(
                description = "Email пользователя",
                example = "example@test.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Пароль пользователя",
                example = "password",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String password) {
}