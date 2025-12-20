package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.mapper.OrderItemMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderItemRepository;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper itemMapper;

    @Override
    public OrderItemDto addItem(UUID userId, UUID orderId, OrderItemCreateDto dto) {
        Order order = findOrderByIdOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            log.warn("Only order owner can add items. orderId={}, userId={}", orderId, userId);
            throw new ConflictException("Only order owner can add items");
        }
        if (order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.ORDERED)
                || order.getStatus().equals(OrderStatus.CANCELED)) {
            log.warn("The item cannot be added to the order due to its status. " +
                    "orderId={}, orderStatus={}, userId={}", orderId, order.getStatus(), userId);
            throw new ConflictException(
                    "The item cannot be added to the order due to its status. orderStatus=" + order.getStatus()
            );
        }
        OrderItem item = itemMapper.toEntity(dto);
        item.setOrder(order);
        OrderItem savedItem = itemRepository.save(item);
        log.debug("Item saved to order. orderId={}, itemId={}", orderId, savedItem.getId());
        return itemMapper.toDto(savedItem);
    }

    private Order findOrderByIdOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(()-> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId);
        });
    }
}
