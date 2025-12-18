package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderService {
    OrderDto create(OrderCreateDto createDto, UUID userId);

    OrderDto update(OrderUpdateDto updateDto, UUID orderId);

    void delete(UUID orderId, UUID userId);

    OrderDto getById(UUID orderId, UUID userId);

    PageResponse<OrderDto> getAll(UUID userId, int page, int size, OrderStatus status, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd);
}
