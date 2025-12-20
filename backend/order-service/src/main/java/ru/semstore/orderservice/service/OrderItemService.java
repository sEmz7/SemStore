package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;

import java.util.UUID;

public interface OrderItemService {

    OrderItemDto addItem(UUID userId, UUID orderId, OrderItemCreateDto dto);

    OrderItemDto getItemById(UUID userId, UUID orderId, UUID itemId);
}
