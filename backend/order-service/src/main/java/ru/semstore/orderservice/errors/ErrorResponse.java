package ru.semstore.orderservice.errors;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.semstore.orderservice.errors.exceptions.ErrorCode;

import java.time.LocalDateTime;

@Schema(description = "DTO для ошибок")
@Data
public class ErrorResponse {
    @Schema(description = "Сообщение об ошибке", example = "user not found")
    private String message;

    @Schema(description = "Дата ошибки", example = "2025-12-17T21:03:50.599901")
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private ErrorCode code;

    public ErrorResponse(String message, ErrorCode code) {
        this.message = message;
        this.date = LocalDateTime.now();
        this.code = code;
    }
}
