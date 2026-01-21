package ru.semstore.userservice.dto.user;

import jakarta.validation.constraints.NotNull;
import ru.semstore.userservice.model.UserRole;

public record UserRoleUpdateDto(
        @NotNull
        UserRole newRole
) {
}
