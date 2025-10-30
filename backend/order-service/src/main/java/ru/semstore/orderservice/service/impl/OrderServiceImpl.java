package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.OrderUpdateDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.kafka.producer.KafkaProducer;
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
    private final KafkaProducer kafka;

    @Override
    public OrderDto create(OrderCreateDto createDto, UUID userId) {
        Order order = orderMapper.toEntity(createDto);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.debug("Saved order with id={}", savedOrder.getId());

        kafka.sendOrderToCheck(savedOrder);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto update(OrderUpdateDto updateDto, UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId);
        });
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.ORDERED) {
            log.warn("Order status is {}, address cannot be changed. orderId={}", order.getStatus(), orderId);
            throw new ConflictException("Order status is " + order.getStatus() +
                    ", address cannot be changed. orderId=" + orderId);
        }
        order.setAddressId(updateDto.addressId());
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        log.debug("Order updated. orderId={}", order.getId());

        kafka.sendOrderToCheck(order);
        return orderMapper.toDto(order);
    }
}
