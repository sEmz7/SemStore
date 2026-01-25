package ru.semstore.userservice.exception;

public class ConflictException extends AppException {
    public ConflictException(String message, ErrorCode code) {
        super(message, code);
    }
}
