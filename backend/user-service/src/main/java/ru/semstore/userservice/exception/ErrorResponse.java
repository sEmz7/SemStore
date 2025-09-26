package ru.semstore.userservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorResponse(
        @Schema(description = "Текст ошибки", example = "error message")
        String message
) {
}
