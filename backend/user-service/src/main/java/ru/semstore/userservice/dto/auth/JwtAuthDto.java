package ru.semstore.userservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для передачи токенов")
public class JwtAuthDto {
    @Schema(description = "Токен доступа",
            example = "jwt token")
    private String token;

    @Schema(description = "Токен обновления",
    example = "refresh token")
    private String refreshToken;
}
