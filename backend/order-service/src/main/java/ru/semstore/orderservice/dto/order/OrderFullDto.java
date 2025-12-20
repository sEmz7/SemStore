package ru.semstore.orderservice.dto.order;

import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderFullDto(
        UUID id,
        UUID userId,
        UUID addressId,
        OrderStatus status,
        LocalDateTime createdDate,
        List<OrderItemDto> items
) {
}
