package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.orderItem.ItemPriceUpdateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.ErrorCode;
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

/**
 * Реализация сервиса управления товарами внутри заказа {@link OrderItemService}.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper itemMapper;

    /**
     * Набор статусов заказа, при которых изменение его содержимого запрещено.
     */
    private static final EnumSet<OrderStatus> NOT_MODIFIABLE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.IN_CHECK, OrderStatus.CANCELED, OrderStatus.AWAITING_PAYMENT,
                    OrderStatus.DELIVERING, OrderStatus.COMPLETED);

    /**
     * Добавляет новый товар в заказ пользователя.
     *
     * <p>Проверяет существование заказа, принадлежность заказа пользователю
     * и возможность изменения заказа по его статусу. В случае успеха
     * сохраняет товар и возвращает сохранённую сущность.</p>
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа, в который добавляется товар
     * @param dto     данные создаваемого товара
     * @return созданный товар
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      если пользователь не владелец заказа
     *                                или заказ нельзя изменять по статусу
     */
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

    /**
     * Возвращает товар заказа по идентификатору.
     *
     * <p>Загружает товар вместе с заказом, проверяет принадлежность товара
     * указанному заказу и принадлежность заказа пользователю.</p>
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара в заказе
     * @return товар заказа
     * @throws ItemNotFoundException если товар не найден
     * @throws ConflictException     если товар не принадлежит указанному заказу
     *                               или пользователь не владелец заказа
     */
    @Transactional(readOnly = true)
    @Override
    public OrderItemDto getItemById(UUID userId, UUID orderId, UUID itemId) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);

        validateItemBelongsToOrder(item, orderId);
        validateOrderOwner(item.getOrder(), userId);

        return itemMapper.toDto(item);
    }

    /**
     * Удаляет товар из заказа.
     *
     * <p>Загружает товар вместе с заказом, проверяет принадлежность товара
     * заказу, принадлежность заказа пользователю и возможность изменения
     * заказа по его статусу. В случае успеха удаляет товар.</p>
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор удаляемого товара
     * @throws ItemNotFoundException если товар не найден
     * @throws ConflictException     если товар не принадлежит заказу,
     *                               пользователь не владелец заказа
     *                               или заказ нельзя изменять по статусу
     */
    @Override
    public void delete(UUID userId, UUID orderId, UUID itemId) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);

        validateItemBelongsToOrder(item, orderId);
        validateOrderOwner(item.getOrder(), userId);
        validateOrderIsModifiable(item.getOrder(), userId);

        itemRepository.deleteById(itemId);
        log.debug("Item deleted. userId={}, orderId={}, itemId={}", userId, orderId, itemId);
    }

    /**
     * Обновляет данные товара в заказе.
     *
     * <p>Загружает товар вместе с заказом, проверяет принадлежность товара
     * заказу, принадлежность заказа пользователю и возможность изменения
     * заказа по его статусу. После успешной проверки применяет изменения
     * и сохраняет товар.</p>
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара
     * @param dto     данные для обновления товара
     * @return обновлённый товар
     * @throws ItemNotFoundException если товар не найден
     * @throws ConflictException     если товар не принадлежит заказу,
     *                               пользователь не владелец заказа
     *                               или заказ нельзя изменять по статусу
     */
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

    /**
     * Обновляет цену товара в заказе администратором.
     *
     * <p>Загружает товар вместе с заказом, проверяет принадлежность товара указанному заказу
     * и проверяет, что заказ находится в статусе {@link OrderStatus#IN_CHECK}.
     * После успешной проверки сохраняет новую цену товара.</p>
     *
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара в заказе
     * @param dto     DTO с новой ценой товара
     * @return товар заказа с обновлённой ценой
     * @throws ItemNotFoundException если товар не найден
     * @throws ConflictException     если товар не принадлежит заказу
     *                               или заказ не в статусе {@link OrderStatus#IN_CHECK}
     */
    @Override
    public OrderItemDto updateItemPrice(UUID orderId, UUID itemId, ItemPriceUpdateDto dto) {
        OrderItem item = findItemByIdWithOrderOrThrow(itemId);
        validateItemBelongsToOrder(item, orderId);
        if (!item.getOrder().getStatus().equals(OrderStatus.IN_CHECK)) {
            log.warn("Order status must be IN_CHECK before pricing. orderId={}, orderStatus={}",
                    orderId, item.getOrder().getStatus());
            throw new ConflictException("Order status must be IN_CHECK before pricing.",
                    ErrorCode.ORDER_STATUS_NOT_IN_CHECK);
        }
        item.setPrice(dto.price());
        itemRepository.save(item);
        log.debug("Item price updated to={}. orderId-{}, itemId={}", dto.price(), orderId, itemId);
        return itemMapper.toDto(item);
    }

    /**
     * Возвращает товар по идентификатору вместе с его заказом
     * или выбрасывает исключение, если товар не найден.
     *
     * @param itemId идентификатор товара
     * @return товар с загруженным заказом
     * @throws ItemNotFoundException если товар не найден
     */
    private OrderItem findItemByIdWithOrderOrThrow(UUID itemId) {
        return itemRepository.findItemByIdWithOrder(itemId).orElseThrow(() -> {
            log.warn("Item not found. itemId={}", itemId);
            return new ItemNotFoundException("Item not found", ErrorCode.ITEM_NOT_FOUND);
        });
    }

    /**
     * Возвращает заказ по идентификатору или выбрасывает исключение,
     * если заказ не найден.
     *
     * @param orderId идентификатор заказа
     * @return заказ
     * @throws OrderNotFoundException если заказ не найден
     */
    private Order findOrderByIdOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(()-> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId, ErrorCode.ORDER_NOT_FOUND);
        });
    }

    /**
     * Проверяет, что заказ принадлежит указанному пользователю.
     *
     * @param order  заказ
     * @param userId идентификатор пользователя
     * @throws ConflictException если заказ принадлежит другому пользователю
     */
    private void validateOrderOwner(Order order, UUID userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("Only order owner can access/modify items. orderId={}, userId={}",
                    order.getId(), userId);
            throw new ConflictException("Only order owner can get order info", ErrorCode.ORDER_OWNER_CONFLICT);
        }
    }

    /**
     * Проверяет, что заказ можно изменять в текущем статусе.
     *
     * @param order  заказ
     * @param userId идентификатор пользователя (для логирования)
     * @throws ConflictException если статус заказа не допускает изменений
     */
    private void validateOrderIsModifiable(Order order, UUID userId) {
        if (NOT_MODIFIABLE_STATUSES.contains(order.getStatus())) {
            log.warn("The order cannot be modified due to its status. orderId={}, orderStatus={}, userId={}",
                    order.getId(), order.getStatus(), userId);
            throw new ConflictException(
                    "The order cannot be modified due to its status. orderStatus=" + order.getStatus(),
                    ErrorCode.ORDER_STATUS_NOT_MODIFIABLE
            );
        }
    }

    /**
     * Проверяет, что товар принадлежит указанному заказу.
     *
     * @param item    товар
     * @param orderId ожидаемый идентификатор заказа
     * @throws ConflictException если товар привязан к другому заказу
     */
    private void validateItemBelongsToOrder(OrderItem item, UUID orderId) {
        UUID actualOrderId = item.getOrder().getId();
        if (!orderId.equals(actualOrderId)) {
            log.warn("Item does not belong to this order. expectedOrderId={}, actualOrderId={}, itemId={}",
                    orderId, actualOrderId, item.getId());
            throw new ConflictException("Item does not belong to this order", ErrorCode.ITEM_NOT_IN_ORDER);
        }
    }
}
