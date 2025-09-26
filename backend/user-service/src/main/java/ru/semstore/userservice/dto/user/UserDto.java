package ru.semstore.userservice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO для просмотра данных пользователя")
public record UserDto(
        @Schema(description = "ID пользователя", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Email пользователя", example = "example@test.com")
        String email
) {
}
