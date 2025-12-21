package ru.semstore.orderservice.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO для полного просмотра заказа")
public record OrderFullDto(
        @Schema(description = "ID заказа", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
        UUID id,

        @Schema(description = "ID пользователя", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
        UUID userId,

        @Schema(description = "ID адреса", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
        UUID addressId,

        @Schema(description = "Статус заказа", example = "CREATED")
        OrderStatus status,

        @Schema(description = "Дата создания заказа", example = "2025-12-17T21:03:50.599901")
        LocalDateTime createdDate,

        @Schema(description = "Товары заказа")
        List<OrderItemDto> items
) {
}
