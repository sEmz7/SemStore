package ru.semstore.orderservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.UUID;

@Tag(name = "Товары заказа", description = "Управление товарами в заказе для пользователя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController {
    private final String USER_ID_HEADER = "X-User-Id";
    private final OrderItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemDto create(@RequestHeader(USER_ID_HEADER) UUID userId,
                               @PathVariable("orderId") UUID orderId,
                               @Valid @RequestBody OrderItemCreateDto dto) {
        return itemService.addItem(userId, orderId, dto);
    }

    @GetMapping("/{itemId}")
    public OrderItemDto getItemById(@RequestHeader(USER_ID_HEADER) UUID userId,
                                    @PathVariable("orderId") UUID orderId,
                                    @PathVariable("itemId") UUID itemId) {
        return itemService.getItemById(userId, orderId, itemId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(USER_ID_HEADER) UUID userId,
                       @PathVariable("orderId") UUID orderId,
                       @PathVariable("itemId") UUID itemId) {
        itemService.delete(userId, orderId, itemId);
    }
}
