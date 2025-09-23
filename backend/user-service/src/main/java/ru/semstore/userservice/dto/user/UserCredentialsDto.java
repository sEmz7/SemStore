package ru.semstore.userservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password) {
}
