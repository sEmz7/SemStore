package ru.semstore.userservice.dto.jwt;

import lombok.Data;

@Data
public class JwtAuthDto {
    private String token;
    private String refreshToken;
}
