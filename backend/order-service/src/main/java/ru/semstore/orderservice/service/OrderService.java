package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;

import java.util.UUID;

public interface OrderService {
    OrderDto create(OrderCreateDto createDto, UUID userId);
}
