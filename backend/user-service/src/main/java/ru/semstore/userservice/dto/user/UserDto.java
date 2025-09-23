package ru.semstore.userservice.dto.user;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email
) {
}
