package ru.semstore.userservice.exception;

public class UserInputException extends RuntimeException {
    public UserInputException(String message) {
        super(message);
    }
}
