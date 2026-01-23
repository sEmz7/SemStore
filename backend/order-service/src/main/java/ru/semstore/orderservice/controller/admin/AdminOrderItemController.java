package ru.semstore.orderservice.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.orderItem.ItemPriceUpdateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.errors.ErrorResponse;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.UUID;

@Tag(name = "admin: Товары заказа", description = "API для управления товарами в заказе для админа")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/orders/{orderId}/items")
@RequiredArgsConstructor
@Validated
public class AdminOrderItemController {
    private final OrderItemService itemService;

    @Operation(
            summary = "Обновить цену товара",
            description = "Обновляет цену товара в заказе и возвращает товар",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Товар обновлен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderItemDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ или товар не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PatchMapping("/{itemId}")
    public OrderItemDto updateItemPrice(@PathVariable("orderId") UUID orderId,
                                        @PathVariable("itemId") UUID itemId,
                                        @Valid @RequestBody ItemPriceUpdateDto dto) {
        return itemService.updateItemPrice(orderId, itemId, dto);
    }
}
