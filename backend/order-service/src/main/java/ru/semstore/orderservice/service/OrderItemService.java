package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;

import java.util.UUID;

public interface OrderItemService {

    OrderItemDto addItem(UUID userId, UUID orderId, OrderItemCreateDto dto);

    OrderItemDto getItemById(UUID userId, UUID orderId, UUID itemId);

    void delete(UUID userId, UUID orderId, UUID itemId);

    OrderItemDto update(UUID userId, UUID orderId, UUID itemId, OrderItemUpdateDto dto);
}
