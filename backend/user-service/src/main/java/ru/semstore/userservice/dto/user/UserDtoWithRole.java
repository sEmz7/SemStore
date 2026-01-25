package ru.semstore.userservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.semstore.userservice.model.UserRole;

import java.util.UUID;

@Schema(description = "DTO для просмотра пользователя с его ролью")
public record UserDtoWithRole(
        @Schema(description = "ID пользователя", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Email пользователя", example = "example@test.com")
        String email,

        @Schema(description = "Роль пользователя", example = "ROLE_USER")
        UserRole role) {
}
