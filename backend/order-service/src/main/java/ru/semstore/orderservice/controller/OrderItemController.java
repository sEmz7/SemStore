package ru.semstore.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.errors.ErrorResponse;
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

    @Operation(
            summary = "Добавить товар в заказ",
            description = "Создает товар и прикрепляет его к заказу",
            responses = {
                    @ApiResponse(responseCode = "201", description = "(CREATED) Товар добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderItemDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес-логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemDto create(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
                               @PathVariable("orderId") UUID orderId,
                               @Valid @RequestBody OrderItemCreateDto dto) {
        return itemService.addItem(userId, orderId, dto);
    }

    @Operation(
            summary = "Получить товар по ID",
            description = "Возвращает товар в заказе по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Товар возвращен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderItemDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес-логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{itemId}")
    public OrderItemDto getItemById(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
                                    @PathVariable("orderId") UUID orderId,
                                    @PathVariable("itemId") UUID itemId) {
        return itemService.getItemById(userId, orderId, itemId);
    }

    @Operation(
            summary = "Удалить товар по ID",
            description = "Удаляет товар в заказе по ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "(NO CONTENT) Товар удален"),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес-логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
                       @PathVariable("orderId") UUID orderId,
                       @PathVariable("itemId") UUID itemId) {
        itemService.delete(userId, orderId, itemId);
    }

    @Operation(
            summary = "Обновить товар по ID",
            description = "Обновляет данные в товаре заказа по ID",
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
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес-логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping("/{itemId}")
    public OrderItemDto update(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
                               @PathVariable("orderId") UUID orderId,
                               @PathVariable("itemId") UUID itemId,
                               @Valid @RequestBody OrderItemUpdateDto dto) {
        return itemService.update(userId, orderId, itemId, dto);
    }
}
