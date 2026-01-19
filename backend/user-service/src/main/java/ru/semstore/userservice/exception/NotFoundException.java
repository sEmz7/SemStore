package ru.semstore.userservice.exception;

public class NotFoundException extends AppException {
    public NotFoundException(String message, ErrorCode code) {
        super(message, code);
    }
}
