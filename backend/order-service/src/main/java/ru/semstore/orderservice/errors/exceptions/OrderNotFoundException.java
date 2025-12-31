package ru.semstore.orderservice.errors.exceptions;

public class OrderNotFoundException extends AppException {
    public OrderNotFoundException(String message, ErrorCode code) {
        super(message, code);
    }
}
