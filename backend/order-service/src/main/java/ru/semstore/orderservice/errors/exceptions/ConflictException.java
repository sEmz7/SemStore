package ru.semstore.orderservice.errors.exceptions;

public class ConflictException extends AppException {
    public ConflictException(String message, ErrorCode code) {
        super(message, code);
    }
}
