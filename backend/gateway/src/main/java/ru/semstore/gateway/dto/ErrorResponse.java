package ru.semstore.gateway.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String message;
    private final LocalDateTime date;

    public ErrorResponse(String message, LocalDateTime date) {
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
