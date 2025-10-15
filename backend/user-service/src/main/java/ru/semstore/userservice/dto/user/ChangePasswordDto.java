package ru.semstore.userservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO для изменения пароля")
public record ChangePasswordDto(
        @Schema(
                description = "Старый пароль",
                example = "oldPassword123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String oldPassword,
        @Schema(
                description = "Новый пароль",
                example = "newPassword456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String newPassword) {
}
