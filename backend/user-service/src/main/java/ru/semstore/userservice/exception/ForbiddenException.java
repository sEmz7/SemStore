package ru.semstore.userservice.exception;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message, ErrorCode code) {
        super(message, code);
    }
}
