package ru.semstore.orderservice.service;

import ru.semstore.orderservice.dto.orderItem.ItemPriceUpdateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;

import java.util.UUID;

/**
 * Сервис для управления товарами внутри заказа пользователя.
 */
public interface OrderItemService {

    /**
     * Добавляет новый товар в заказ пользователя.
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param dto     данные создаваемого товара
     * @return созданный товар
     */
    OrderItemDto addItem(UUID userId, UUID orderId, OrderItemCreateDto dto);

    /**
     * Возвращает товар заказа по идентификатору.
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара в заказе
     * @return товар заказа
     */
    OrderItemDto getItemById(UUID userId, UUID orderId, UUID itemId);

    /**
     * Удаляет товар из заказа.
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор удаляемого товара
     */
    void delete(UUID userId, UUID orderId, UUID itemId);

    /**
     * Обновляет данные товара в заказе.
     *
     * @param userId  идентификатор пользователя (владельца заказа)
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара
     * @param dto     данные для обновления товара
     * @return обновлённый товар
     */
    OrderItemDto update(UUID userId, UUID orderId, UUID itemId, OrderItemUpdateDto dto);

    /**
     * Обновляет цену товара в заказе администратором.
     *
     * @param orderId идентификатор заказа
     * @param itemId  идентификатор товара в заказе
     * @param dto     DTO с новой ценой товара
     * @return товар заказа с обновлённой ценой
     */
    OrderItemDto updateItemPrice(UUID orderId, UUID itemId, ItemPriceUpdateDto dto);
}
