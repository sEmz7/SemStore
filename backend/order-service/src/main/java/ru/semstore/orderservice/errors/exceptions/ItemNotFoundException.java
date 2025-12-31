package ru.semstore.orderservice.errors.exceptions;

public class ItemNotFoundException extends AppException {
    public ItemNotFoundException(String message, ErrorCode code) {
        super(message, code);
    }
}
