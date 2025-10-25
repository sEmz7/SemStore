package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.mapper.OrderMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderDto create(OrderCreateDto createDto, UUID userId) {
        Order order = orderMapper.toEntity(createDto);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.debug("Saved order with id={}", savedOrder.getId());
        return orderMapper.toDto(savedOrder);
    }
}
