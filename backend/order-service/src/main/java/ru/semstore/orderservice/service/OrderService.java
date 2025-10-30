package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.OrderUpdateDto;

import java.util.UUID;

public interface OrderService {
    OrderDto create(OrderCreateDto createDto, UUID userId);

    OrderDto update(OrderUpdateDto updateDto, UUID orderId);
}
