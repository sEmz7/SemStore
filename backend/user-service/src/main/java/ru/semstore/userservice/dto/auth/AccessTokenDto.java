package ru.semstore.userservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO для access токена")
public record AccessTokenDto(
        @Schema(description = "access токен")
        String token) { }