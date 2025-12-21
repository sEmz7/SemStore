package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.ItemNotFoundException;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.mapper.OrderItemMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderItemRepository;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.EnumSet;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper itemMapper;

    private static final EnumSet<OrderStatus> NOT_MODIFIABLE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.ORDERED, OrderStatus.CANCELED);

    @Override
    public OrderItemDto addItem(UUID userId, UUID orderId, OrderItemCreateDto dto) {
        Order order = findOrderByIdOrThrow(orderId);

        validateOrderOwner(order, userId);
        validateOrderIsModifiable(order, userId);

        OrderItem item = itemMapper.toEntity(dto);
        item.setOrder(order);
        OrderItem savedItem = itemRepository.save(item);
        log.debug("Item saved to order. orderId={}, itemId={}", orderId, savedItem.getId());
        return itemMapper.toDto(savedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderItemDto getItemById(UUID userId, UUID orderId, UUID itemId) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);

        validateItemBelongsToOrder(item, orderId);
        validateOrderOwner(item.getOrder(), userId);

        return itemMapper.toDto(item);
    }

    @Override
    public void delete(UUID userId, UUID orderId, UUID itemId) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);

        validateItemBelongsToOrder(item, orderId);
        validateOrderOwner(item.getOrder(), userId);
        validateOrderIsModifiable(item.getOrder(), userId);

        itemRepository.deleteById(itemId);
        log.debug("Item deleted. userId={}, orderId={}, itemId={}", userId, orderId, itemId);
    }

    @Override
    public OrderItemDto update(UUID userId, UUID orderId, UUID itemId, OrderItemUpdateDto dto) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);

        validateItemBelongsToOrder(item, orderId);
        validateOrderOwner(item.getOrder(), userId);
        validateOrderIsModifiable(item.getOrder(), userId);

        itemMapper.update(item, dto);

        itemRepository.save(item);
        log.debug("Item updated. userId={}, orderId={}, itemId={}", userId, orderId, itemId);
        return itemMapper.toDto(item);
    }

    private OrderItem findItemByIdWithOrderOrThrow(UUID itemId) {
        return itemRepository.findItemByIdWithOrder(itemId).orElseThrow(() -> {
            log.warn("Item not found. itemId={}", itemId);
            return new ItemNotFoundException("Item not found");
        });
    }

    private Order findOrderByIdOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(()-> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId);
        });
    }

    private void validateOrderOwner(Order order, UUID userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("Only order owner can access/modify items. orderId={}, userId={}",
                    order.getId(), userId);
            throw new ConflictException("Only order owner can get order info");
        }
    }

    private void validateOrderIsModifiable(Order order, UUID userId) {
        if (NOT_MODIFIABLE_STATUSES.contains(order.getStatus())) {
            log.warn("The order cannot be modified due to its status. orderId={}, orderStatus={}, userId={}",
                    order.getId(), order.getStatus(), userId);
            throw new ConflictException(
                    "The order cannot be modified due to its status. orderStatus=" + order.getStatus()
            );
        }
    }

    private void validateItemBelongsToOrder(OrderItem item, UUID orderId) {
        UUID actualOrderId = item.getOrder().getId();
        if (!orderId.equals(actualOrderId)) {
            log.warn("Item does not belong to this order. expectedOrderId={}, actualOrderId={}, itemId={}",
                    orderId, actualOrderId, item.getId());
            throw new ConflictException("Item does not belong to this order");
        }
    }
}
