package ru.semstore.userservice.dto.jwt;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDto {
    @NotBlank
    private String refreshToken;
}
