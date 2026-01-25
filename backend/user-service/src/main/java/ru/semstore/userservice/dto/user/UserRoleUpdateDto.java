package ru.semstore.userservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.semstore.userservice.model.UserRole;

@Schema(description = "DTO для обновления роли пользователя")
public record UserRoleUpdateDto(
        @Schema(description = "Новая роль", example = "ROLE_USER")
        @NotNull
        UserRole newRole
) {
}
