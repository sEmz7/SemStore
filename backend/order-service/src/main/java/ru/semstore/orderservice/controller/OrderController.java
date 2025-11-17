package ru.semstore.orderservice.controller;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.OrderUpdateDto;
import ru.semstore.orderservice.errors.ErrorResponse;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Заказы", description = "Управление заказами пользователя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/orders")
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final String USER_ID_HEADER = "X-User-Id";
    private final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать заказ",
            description = "Создаёт новый заказ со статусом PENDING и отправляет событие в Kafka для проверки.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Заказ создан",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные запроса"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Конфликт бизнес-логики")
            }
    )
    public OrderDto create(@Valid @RequestBody OrderCreateDto dto,
                           @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.create(dto, userId);
    }

    @PatchMapping("/{orderId}")
    @Operation(
            summary = "Обновить заказ",
            description = "Обновляет адрес заказа. Недоступно, если заказ в статусах PAID/ORDERED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Заказ обновлён",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные запроса"),
                    @ApiResponse(responseCode = "404", description = "Заказ не найден"),
                    @ApiResponse(responseCode = "409", description = "Адрес менять нельзя для текущего статуса")
            }
    )
    public OrderDto update(@Valid @RequestBody OrderUpdateDto dto, @PathVariable("orderId") UUID orderId) {
        return orderService.update(dto, orderId);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить заказ",
            description = "Удаляет заказ владельца. Недоступно для статусов PAID/ORDERED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Удалено"),
                    @ApiResponse(responseCode = "404", description = "Заказ не найден"),
                    @ApiResponse(responseCode = "409", description = "Удаление запрещено по статусу или" +
                            " удалить может только владелец")
            }
    )
    public void delete(@PathVariable("orderId") UUID orderId,
                       @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        orderService.delete(orderId, userId);
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Получить заказ по id",
            description = "Возвращает заказ, если запрашивающий — его владелец.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "404", description = "Заказ не найден"),
                    @ApiResponse(responseCode = "409", description = "Доступ только владельцу")
            }
    )
    public OrderDto getById(@PathVariable("orderId") UUID orderId,
                            @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.getById(orderId, userId);
    }

    @GetMapping
    @Operation(
            summary = "Получить список заказов пользователя",
            description = "Постраничная выборка заказов пользователя с фильтрами по статусу и диапазону дат.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = OrderDto.class)))),
                    @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
            }
    )
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
    public List<OrderDto> getAllOrders(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") int page,
                                       @Positive @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) OrderStatus status,
                                       @RequestParam(required = false)
                                           @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
                                       @RequestParam(required = false)
                                           @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd) {
        return orderService.getAll(userId, page, size, status, rangeStart, rangeEnd);
    }
}
