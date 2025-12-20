package ru.semstore.orderservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public OrderItemDto create(@RequestHeader(USER_ID_HEADER) UUID userId,
                               @PathVariable("orderId") UUID orderId,
                               @Valid @RequestBody OrderItemCreateDto dto) {
        return itemService.addItem(userId, orderId, dto);
    }
}
