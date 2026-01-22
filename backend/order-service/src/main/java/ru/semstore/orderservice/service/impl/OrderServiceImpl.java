package ru.semstore.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.orderservice.dto.order.*;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.ErrorCode;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.kafka.producer.KafkaProducer;
import ru.semstore.orderservice.mapper.OrderMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Реализация сервиса заказов {@link OrderService}.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaProducer kafka;

    /**
     * Набор статусов заказа, при которых изменение его содержимого запрещено.
     */
    private static final EnumSet<OrderStatus> NOT_MODIFIABLE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.IN_CHECK, OrderStatus.AWAITING_PAYMENT,
                    OrderStatus.DELIVERING, OrderStatus.COMPLETED);

    /**
     * Создаёт новый заказ пользователя.
     *
     * <p>Устанавливает владельца, статус {@link OrderStatus#PENDING} и дату создания.
     * После сохранения отправляет заказ на проверку в user service через Kafka.</p>
     *
     * @param createDto данные для создания заказа
     * @param userId    идентификатор владельца заказа
     * @return созданный заказ
     */
    @Override
    public OrderShortDto create(OrderCreateDto createDto, UUID userId) {
        Order order = orderMapper.toEntity(createDto);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.debug("Saved order with id={}", savedOrder.getId());

        kafka.sendOrderToCheck(savedOrder);
        return orderMapper.toShortDto(savedOrder);
    }

    /**
     * Обновляет заказ.
     *
     * <p>Обновление разрешено для заказов в статусах
     * {@link OrderStatus#CREATED}, {@link OrderStatus#PENDING} и {@link OrderStatus#CANCELED}.
     * После обновления заказ переводится в статус {@link OrderStatus#PENDING}
     * и отправляется на повторную проверку.</p>
     *
     * @param updateDto данные для обновления заказа
     * @param orderId   идентификатор заказа
     * @param userId идентификатор пользователя
     * @return обновлённый заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      в случае конфликта
     */
    @Override
    public OrderShortDto update(OrderUpdateDto updateDto, UUID orderId, UUID userId) {
        Order order = findOrderByIdOrThrow(orderId);
        validateOrderOwnerOrThrow(order, userId);
        validateOrderStatusModifiable(order);
        orderMapper.update(order, updateDto);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        log.debug("Order updated. orderId={}", order.getId());

        kafka.sendOrderToCheck(order);
        return orderMapper.toShortDto(order);
    }

    /**
     * Удаляет заказ пользователя.
     *
     * <p>Удаление доступно только владельцу заказа и разрешено
     * для статусов {@link OrderStatus#CREATED}, {@link OrderStatus#PENDING} и {@link OrderStatus#CANCELED}</p>
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор владельца заказа
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      если пользователь не владелец или удаление запрещено
     */
    @Override
    public void delete(UUID orderId, UUID userId) {
        Order order = findOrderByIdOrThrow(orderId);
        validateOrderOwnerOrThrow(order, userId);
        validateOrderStatusModifiable(order);
        orderRepository.deleteById(orderId);
        log.debug("Order deleted. orderId={}", orderId);
    }

    /**
     * Возвращает заказ по идентификатору.
     *
     * <p>Доступ разрешён только владельцу заказа.</p>
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор владельца заказа
     * @return заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      в случае конфликта
     */
    @Transactional(readOnly = true)
    @Override
    public OrderFullDto getById(UUID orderId, UUID userId) {
        Order order = findOrderWithItemsByIdOrThrow(orderId);
        validateOrderOwnerOrThrow(order, userId);
        return orderMapper.toFullDto(order);
    }

    /**
     * Возвращает постраничную выборку заказов пользователя.
     *
     * <p>Поддерживает фильтрацию по статусу и диапазону дат.
     * Сортировка выполняется по дате создания по убыванию.</p>
     *
     * @param userId     идентификатор владельца заказов
     * @param page       номер страницы
     * @param size       размер страницы
     * @param status     фильтр по статусу (может быть {@code null})
     * @param rangeStart начало диапазона дат (может быть {@code null})
     * @param rangeEnd   конец диапазона дат (может быть {@code null})
     * @return страница заказов пользователя
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrderShortDto> getAll(UUID userId, int page, int size, OrderStatus status, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Order> ordersPage = orderRepository.findAllBySortAndUserId(pageable, userId, status, rangeStart, rangeEnd);
        return PageResponse.from(ordersPage.map(orderMapper::toShortDto));
    }

    /**
     * Подтверждает заказ, переводя его статус в IN_CHECK.
     *
     * <p>Этот метод доступен только для заказов в статусе {@link OrderStatus#CREATED}.
     * Метод также проверяет, что заказ содержит хотя бы один товар.</p>
     *
     * @param orderId идентификатор заказа
     * @param userId идентификатор пользователя
     * @return обновленный заказ в виде {@link OrderFullDto} с новым статусом
     * @throws OrderNotFoundException если заказ не найден по указанному идентификатору
     * @throws ConflictException если заказ не может быть подтвержден из-за неправильного статуса или отсутствия товаров
     */
    @Override
    public OrderFullDto confirmOrder(UUID orderId, UUID userId) {
        Order order = findOrderWithItemsByIdOrThrow(orderId);
        validateOrderOwnerOrThrow(order, userId);
        if (order.getStatus() != OrderStatus.CREATED) {
            log.warn("The order must be in the CREATED status to be confirmed");
            throw new ConflictException("The order must be in the CREATED status to be confirmed",
                    ErrorCode.ORDER_STATUS_NOT_CREATED);
        }
        if (order.getItems().isEmpty()) {
            log.warn("The order must contain at least 1 item");
            throw new ConflictException("The order must contain at least 1 item", ErrorCode.ORDER_IS_EMPTY);
        }
        order.setStatus(OrderStatus.IN_CHECK);
        orderRepository.save(order);
        return orderMapper.toFullDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrderShortDto> getAllOrdersForCheck(int page, int size, OrderStatus status,
                                                            LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        status = status == null ? OrderStatus.IN_CHECK : status;
        Page<Order> ordersPage = orderRepository.finaAllBySort(pageable, status, rangeStart, rangeEnd);
        return PageResponse.from(ordersPage.map(orderMapper::toShortDto));
    }

    @Transactional(readOnly = true)
    @Override
    public OrderFullDto getOrderByIdForAdmin(UUID orderId) {
        return orderMapper.toFullDto(findOrderWithItemsByIdOrThrow(orderId));
    }

    @Override
    public OrderFullDto submitOrderForPayment(UUID orderId) {
        Order order = findOrderWithItemsByIdOrThrow(orderId);
        if (!order.getStatus().equals(OrderStatus.IN_CHECK)) {
            log.warn("The order status must be IN_CHECK. orderId={}, orderStatus={}", orderId, order.getStatus());
            throw new ConflictException("The order status must be IN_CHECK.", ErrorCode.ORDER_STATUS_NOT_IN_CHECK);
        }

        BigDecimal totalPrice = new BigDecimal(0);
        for(OrderItem item: order.getItems()) {
            if (item.getPrice() == null) {
                log.warn("All items must have a price. orderId={}", order.getId());
                throw new ConflictException("All items must have a price.", ErrorCode.ITEMS_HAVE_NOT_PRICE);
            }
            totalPrice = totalPrice.add(item.getPrice());
        }

        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
        log.debug("Order status changed to={}. orderId={}", order.getStatus(), order.getId());
        return orderMapper.toFullDto(order);
    }

    /**
     * Возвращает заказ по идентификатору или выбрасывает исключение.
     *
     * @param orderId идентификатор заказа
     * @return заказ
     * @throws OrderNotFoundException если заказ не найден
     */
    private Order findOrderByIdOrThrow(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId, ErrorCode.ORDER_NOT_FOUND);
        });
    }

    /**
     * Возвращает заказ с товарами в нем по идентификатору или выбрасывает исключение.
     *
     * @param orderId идентификатор заказа
     * @return заказ
     * @throws OrderNotFoundException если заказ не найден
     */
    private Order findOrderWithItemsByIdOrThrow(UUID orderId) {
        return orderRepository.findOrderByIdWithItems(orderId).orElseThrow(() -> {
            log.warn("Order with items not found. orderId={}", orderId);
            return new OrderNotFoundException("Order not found. orderId=" + orderId, ErrorCode.ORDER_NOT_FOUND);
        });
    }

    /**
     * Проверяет, что владелец заказа текущий пользователь
     *
     * @param order заказ
     * @param userId идентификатор пользователя
     * @throws ConflictException если пользователь не является владельцем заказа
     */
    private void validateOrderOwnerOrThrow(Order order, UUID userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("Only owner can get order. orderId={}", order.getId());
            throw new ConflictException("Only owner can get order. orderId=" + order.getId(),
                    ErrorCode.ORDER_OWNER_CONFLICT);
        }
    }

    /**
     * Проверяет, что статус заказа позволяет редактировать заказ
     *
     * @param order заказ
     * @throws ConflictException если статус заказа не позволяет его редактирование
     */
    private void validateOrderStatusModifiable(Order order) {
        if (NOT_MODIFIABLE_STATUSES.contains(order.getStatus())) {
            log.warn("Order cannot be changed. orderId={}", order.getId());
            throw new ConflictException("Order cannot be changed. orderId=" + order.getId(),
                    ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
    }
}