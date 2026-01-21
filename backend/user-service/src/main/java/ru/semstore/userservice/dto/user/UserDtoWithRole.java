package ru.semstore.userservice.dto.user;

import ru.semstore.userservice.model.UserRole;

import java.util.UUID;

public record UserDtoWithRole(
        UUID id,
        String email,
        UserRole role) {
}
