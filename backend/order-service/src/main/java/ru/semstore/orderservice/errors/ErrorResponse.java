package ru.semstore.orderservice.errors;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String message;

    @JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss")
    private LocalDateTime date;

    public ErrorResponse(String message) {
        this.message = message;
        this.date = LocalDateTime.now();
    }
}
