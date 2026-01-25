package ru.semstore.userservice.exception;

public abstract class AppException extends RuntimeException {
    private final ErrorCode code;

    public AppException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
