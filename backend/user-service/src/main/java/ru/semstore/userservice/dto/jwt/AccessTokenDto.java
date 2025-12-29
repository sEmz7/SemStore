package ru.semstore.userservice.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO для access токена")
public record AccessTokenDto(
        @Schema(description = "access токен")
        String token) { }