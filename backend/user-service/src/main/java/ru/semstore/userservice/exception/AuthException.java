package ru.semstore.userservice.exception;

public class AuthException extends AppException {
    public AuthException(String message, ErrorCode code) {
        super(message, code);
    }
}
