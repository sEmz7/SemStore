package ru.semstore.userservice.service;

import ru.semstore.userservice.model.User;

public interface VerificationService {

    void createVerificationCode(User user);
}
