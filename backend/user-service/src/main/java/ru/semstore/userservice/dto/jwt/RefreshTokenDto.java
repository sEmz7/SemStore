package ru.semstore.userservice.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для передачи токена обновления")
public class RefreshTokenDto {
    @Schema(description = "refresh token")
    @NotBlank
    private String refreshToken;
}
