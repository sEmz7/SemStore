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
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.ErrorCode;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.kafka.producer.KafkaProducer;
import ru.semstore.orderservice.mapper.OrderMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
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
     * <p>Обновление запрещено для заказов в статусах
     * {@link OrderStatus#PAID} и {@link OrderStatus#ORDERED}.
     * После обновления заказ переводится в статус {@link OrderStatus#PENDING}
     * и отправляется на повторную проверку.</p>
     *
     * @param updateDto данные для обновления заказа
     * @param orderId   идентификатор заказа
     * @return обновлённый заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      если обновление запрещено по статусу
     */
    @Override
    public OrderShortDto update(OrderUpdateDto updateDto, UUID orderId) {
        Order order = findOrderByIdOrThrow(orderId);
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.ORDERED) {
            log.warn("Order status is {}, address cannot be changed. orderId={}", order.getStatus(), orderId);
            throw new ConflictException("Order status is " + order.getStatus() +
                    ", address cannot be changed. orderId=" + orderId, ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
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
     * <p>Удаление доступно только владельцу заказа и запрещено
     * для статусов {@link OrderStatus#PAID} и {@link OrderStatus#ORDERED}.</p>
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор владельца заказа
     * @throws OrderNotFoundException если заказ не найден
     * @throws ConflictException      если пользователь не владелец или удаление запрещено
     */
    @Override
    public void delete(UUID orderId, UUID userId) {
        Order order = findOrderByIdOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            log.warn("Only order owner can delete order. orderId={}", orderId);
            throw new ConflictException("Only order owner can delete order. orderId=" + orderId,
                    ErrorCode.ORDER_OWNER_CONFLICT);
        }
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.ORDERED) {
            log.warn("Order cannot be deleted. orderId={}", orderId);
            throw new ConflictException("Order cannot be deleted. orderId=" + orderId,
                    ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
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
     * @throws ConflictException      если пользователь не владелец заказа
     */
    @Transactional(readOnly = true)
    @Override
    public OrderFullDto getById(UUID orderId, UUID userId) {
        Order order = orderRepository.findOrderByIdWithItems(orderId).orElseThrow(() -> {
            log.warn("Order not found. orderId={}, userId={}", orderId, userId);
            return new OrderNotFoundException("Order not found", ErrorCode.ORDER_NOT_FOUND);
        });
        if (!userId.equals(order.getUserId())) {
            log.warn("Only owner can get order info. orderId={}", orderId);
            throw new ConflictException("Only owner can get order info. orderId=" + orderId,
                    ErrorCode.ORDER_OWNER_CONFLICT);
        }
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
        Page<Order> ordersPage = orderRepository.findAllBySort(pageable, userId, status, rangeStart, rangeEnd);
        return PageResponse.from(ordersPage.map(orderMapper::toShortDto));
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
}
