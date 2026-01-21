package ru.semstore.orderservice.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.orderItem.ItemPriceUpdateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders/{orderId}/items")
@RequiredArgsConstructor
@Validated
public class AdminOrderItemController {
    private final OrderItemService itemService;

    @PatchMapping("/{itemId}")
    public OrderItemDto updateItemPrice(@PathVariable("orderId") UUID orderId,
                                        @PathVariable("itemId") UUID itemId,
                                        @Valid @RequestBody ItemPriceUpdateDto dto) {
        return itemService.updateItemPrice(orderId, itemId, dto);
    }
}
