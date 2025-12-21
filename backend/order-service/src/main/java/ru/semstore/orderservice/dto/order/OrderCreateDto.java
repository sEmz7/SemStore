package ru.semstore.orderservice.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "DTO для создания заказ")
public record OrderCreateDto(
        @Schema(description = "ID адреса", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
        @NotNull
        UUID addressId
) {
}
