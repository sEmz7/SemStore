package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.eventDto.OrderCreatedEvent;
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
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    public OrderDto create(OrderCreateDto createDto, UUID userId) {
        Order order = orderMapper.toEntity(createDto);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.debug("Saved order with id={}", savedOrder.getId());

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(),
                savedOrder.getAddressId());
        kafkaTemplate.send("order-created", orderCreatedEvent);
        return orderMapper.toDto(savedOrder);
    }
}
