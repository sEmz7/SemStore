package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.order.*;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для управления заказами пользователя.
 */
public interface OrderService {
    /**
     * Создаёт новый заказ для пользователя.
     *
     * @param createDto данные для создания заказа
     * @param userId    идентификатор владельца заказа
     * @return созданный заказ
     */
    OrderShortDto create(OrderCreateDto createDto, UUID userId);

    /**
     * Обновляет заказ по идентификатору.
     *
     * @param updateDto данные для обновления заказа
     * @param orderId   идентификатор заказа
     * @param userId   идентификатор пользователя
     * @return обновлённый заказ
     */
    OrderShortDto update(OrderUpdateDto updateDto, UUID orderId, UUID userId);


    /**
     * Удаляет заказ пользователя.
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор пользователя (владельца), выполняющего удаление
     */
    void delete(UUID orderId, UUID userId);

    /**
     * Возвращает заказ по идентификатору.
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор пользователя (владельца)
     * @return заказ
     */
    OrderFullDto getById(UUID orderId, UUID userId);

    /**
     * Возвращает постраничную выборку заказов пользователя с фильтрами.
     *
     * @param userId     идентификатор пользователя (владельца)
     * @param page       номер страницы (0... N)
     * @param size       размер страницы
     * @param status     фильтр по статусу (может быть {@code null})
     * @param rangeStart начало диапазона дат создания (может быть {@code null})
     * @param rangeEnd   конец диапазона дат создания (может быть {@code null})
     * @return страница заказов пользователя
     */
    PageResponse<OrderShortDto> getAll(UUID userId, int page, int size, OrderStatus status, LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd);

    /**
     * Переводит статус заказа в IN_CHECK для проверки товаров администратором.
     *
     * @param orderId идентификатор заказа
     * @param userId идентификатор пользователя
     * @return заказ в виде {@link OrderFullDto} с обновленным статусом
     */
    OrderFullDto confirmOrder(UUID orderId, UUID userId);

    /**
     * Возвращает постраничную выборку всех заказов в системе для администратора.
     *
     * <p>Поддерживается фильтрация по статусу заказа
     * и диапазону дат создания.</p>
     *
     * @param page       номер страницы (0...N)
     * @param size       размер страницы
     * @param status     фильтр по статусу заказа (может быть {@code null})
     * @param rangeStart начало диапазона дат создания (может быть {@code null})
     * @param rangeEnd   конец диапазона дат создания (может быть {@code null})
     * @return страница заказов в сокращённом виде
     */
    PageResponse<OrderShortDto> getAllOrdersForCheck(int page, int size, OrderStatus status,
                                                     LocalDateTime rangeStart, LocalDateTime rangeEnd);


    /**
     * Возвращает заказ по идентификатору для администратора.
     *
     * @param orderId идентификатор заказа
     * @return заказ с полной информацией
     */
    OrderFullDto getOrderByIdForAdmin(UUID orderId);

    /**
     * Выставляет заказ на оплату.
     *
     * <p>Рассчитывает итоговую стоимость заказа
     * и переводит его в статус ожидания оплаты.</p>
     *
     * @param orderId идентификатор заказа
     * @return заказ с обновлённым статусом
     */
    OrderFullDto submitOrderForPayment(UUID orderId);

    /**
     * Завершает заказ.
     *
     * <p>Переводит заказ в финальный завершённый статус
     * после успешного прохождения всех этапов обработки.</p>
     *
     * @param orderId идентификатор заказа
     * @return завершённый заказ
     */
    OrderFullDto completeOrder(UUID orderId);
}