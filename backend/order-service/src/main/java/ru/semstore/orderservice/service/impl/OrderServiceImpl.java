package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.dto.page.PageResponse;
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
        Order order = findOrderByIdOrThrow(orderId);
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

    @Override
    public void delete(UUID orderId, UUID userId) {
        Order order = findOrderByIdOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            log.warn("Only order owner can delete order. orderId={}", orderId);
            throw new ConflictException("Only order owner can delete order. orderId=" + orderId);
        }
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.ORDERED) {
            log.warn("Order cannot be deleted. orderId={}", orderId);
            throw new ConflictException("Order cannot be deleted. orderId=" + orderId);
        }
        orderRepository.deleteById(orderId);
        log.debug("Order deleted. orderId={}", orderId);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getById(UUID orderId, UUID userId) {
        Order order = findOrderByIdOrThrow(orderId);
        if (!userId.equals(order.getUserId())) {
            log.warn("Only owner can get order info. orderId={}", orderId);
            throw new ConflictException("Only owner can get order info. orderId=" + orderId);
        }
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrderDto> getAll(UUID userId, int page, int size, OrderStatus status, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Order> ordersPage = orderRepository.findAllBySort(pageable, userId, status, rangeStart, rangeEnd);
        return PageResponse.from(ordersPage.map(orderMapper::toDto));
    }

    private Order findOrderByIdOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId);
        });
    }
}
