package ru.semstore.orderservice.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.errors.ErrorResponse;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "admin: Заказы", description = "API для управления заказами для админа")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Validated
public class AdminOrderController {
    private final OrderService orderService;

    /**
     * Формат даты и времени для фильтрации заказов
     */
    private final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Operation(
            summary = "Получить все заказы",
            description = "Возвращает все заказы в системе с пагинацией и фильтрацией по статусу и/или дате",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказы возвращены",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = OrderShortDto.class)))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
            })
    @Parameters({
            @Parameter(name = "page", description = "Номер страницы (0..N)",
                    schema = @Schema(minimum = "0", defaultValue = "0", example = "0")),
            @Parameter(name = "size", description = "Размер страницы",
                    schema = @Schema(minimum = "1", defaultValue = "10", example = "10")),
            @Parameter(name = "status", description = "Фильтр по статусу заказа",
                    schema = @Schema(implementation = OrderStatus.class, example = "PENDING")),
            @Parameter(name = "rangeStart", description = "Начало диапазона дат создания",
                    schema = @Schema(type = "string", format = "date-time",
                            example = "2025-01-01 00:00:00"),
                    examples = @ExampleObject(value = "2025-01-01 00:00:00")),
            @Parameter(name = "rangeEnd", description = "Конец диапазона дат создания",
                    schema = @Schema(type = "string", format = "date-time",
                            example = "2025-12-31 23:59:59"),
                    examples = @ExampleObject(value = "2025-12-31 23:59:59"))
    })
    @GetMapping
    public PageResponse<OrderShortDto> getAllOrdersForAdmin(
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd) {
        return orderService.getAllOrdersForCheck(page, size, status, rangeStart, rangeEnd);
    }

    @Operation(
            summary = "Получить заказ по ID", description = "Возвращает заказ по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказ возвращен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderFullDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/{orderId}")
    public OrderFullDto getOrderById(@PathVariable("orderId") UUID orderId) {
        return orderService.getOrderByIdForAdmin(orderId);
    }

    @Operation(
            summary = "Выставить заказ на оплату",
            description = "Считает стоимость товаров в заказе и выставляет статус для оплаты.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказ выставлен на оплату",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderFullDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PatchMapping("/{orderId}/submit")
    public OrderFullDto submitOrderForPayment(@PathVariable("orderId") UUID orderId) {
        return orderService.submitOrderForPayment(orderId);
    }

    @Operation(
            summary = "Завершить заказ",
            description = "Переводит статус заказа в завершенный.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказ завершен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderFullDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Конфликт бизнес логики",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PatchMapping("/{orderId}/complete")
    public OrderFullDto completeOrder(@PathVariable("orderId") UUID orderId) {
        return orderService.completeOrder(orderId);
    }
}
