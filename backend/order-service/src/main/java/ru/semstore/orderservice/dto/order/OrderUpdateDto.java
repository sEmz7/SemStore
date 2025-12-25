package ru.semstore.orderservice.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "DTO для обновления заказа")
public record OrderUpdateDto(
        @Schema(description = "Название заказа", example = "my order", minLength = 2, maxLength = 255)
        @Size(min = 2, max = 255)
        String name,
        @Schema(description = "ID адреса", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
        UUID addressId
) {}