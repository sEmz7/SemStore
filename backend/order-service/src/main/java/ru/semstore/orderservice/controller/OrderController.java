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
import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.errors.ErrorResponse;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST-контроллер для управления заказами пользователя.
 *
 * <p>Контроллер предоставляет API для:
 * <ul>
 *     <li>создания заказов</li>
 *     <li>обновления и удаления заказов</li>
 *     <li>получения одного заказа или списка заказов с фильтрацией</li>
 * </ul>
 *
 * <p>Идентификатор пользователя передаётся через HTTP-заголовок {@code X-User-Id},
 * который устанавливается gateway-сервисом после валидации JWT.</p>
 *
 * <p>Все эндпоинты защищены и требуют валидного JWT-токена.</p>
 */
@Tag(name = "Заказы", description = "Управление заказами пользователя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/orders")
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** HTTP-заголовок с идентификатором пользователя */
    private final String USER_ID_HEADER = "X-User-Id";

    /** Формат даты и времени для фильтрации заказов */
    private final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Создаёт новый заказ пользователя.
     *
     * <p>Заказ создаётся в статусе {@link OrderStatus#PENDING}.
     * После создания инициируется проверка пользователя через user-service.</p>
     *
     * @param dto    DTO с данными для создания заказа
     * @param userId идентификатор пользователя из заголовка {@code X-User-Id}
     * @return созданный заказ
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать заказ",
            description = "Создаёт новый заказ со статусом PENDING и отправляет событие в user service для проверки.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "(CREATED) Заказ создан",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = OrderDto.class))),
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
            })
    public OrderDto create(@Valid @RequestBody OrderCreateDto dto,
                           @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.create(dto, userId);
    }

    /**
     * Обновляет данные заказа.
     *
     * <p>Разрешено только для заказов в статусах,
     * допускающих изменение адреса доставки.</p>
     *
     * @param dto     DTO с обновляемыми данными заказа
     * @param orderId идентификатор заказа
     * @return обновлённый заказ
     */
    @PatchMapping("/{orderId}")
    @Operation(
            summary = "Обновить заказ",
            description = "Обновляет адрес заказа. Недоступно, если заказ в статусах PAID/ORDERED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказ обновлён",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409",
                            description = "(CONFLICT) Адрес менять нельзя для текущего статуса", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public OrderDto update(@Valid @RequestBody OrderUpdateDto dto, @PathVariable("orderId") UUID orderId) {
        return orderService.update(dto, orderId);
    }

    /**
     * Удаляет заказ пользователя.
     *
     * <p>Удаление запрещено для заказов в статусах
     * {@link OrderStatus#PAID} и {@link OrderStatus#ORDERED}.</p>
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор пользователя из заголовка {@code X-User-Id}
     */
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить заказ",
            description = "Удаляет заказ. Недоступно для статусов PAID/ORDERED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "(NO CONTENT) Заказ удален"),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409",
                            description = "(CONFLICT) Удаление запрещено по статусу", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    public void delete(@PathVariable("orderId") UUID orderId,
                       @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        orderService.delete(orderId, userId);
    }

    /**
     * Возвращает заказ по идентификатору.
     *
     * @param orderId идентификатор заказа
     * @param userId  идентификатор пользователя из заголовка {@code X-User-Id}
     * @return заказ пользователя
     */
    @GetMapping("/{orderId}")
    @Operation(
            summary = "Получить заказ по id",
            description = "Возвращает заказ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказ получен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Заказ не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "(CONFLICT) Доступ только владельцу",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    public OrderDto getById(@PathVariable("orderId") UUID orderId,
                            @Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.getById(orderId, userId);
    }

    /**
     * Возвращает постраничный список заказов пользователя.
     *
     * <p>Поддерживается фильтрация по:
     * <ul>
     *     <li>статусу заказа</li>
     *     <li>диапазону дат создания</li>
     * </ul>
     *
     * @param userId     идентификатор пользователя из заголовка {@code X-User-Id}
     * @param page       номер страницы (начиная с 0)
     * @param size       размер страницы
     * @param status     фильтр по статусу заказа (необязательный)
     * @param rangeStart начало диапазона дат создания (необязательный)
     * @param rangeEnd   конец диапазона дат создания (необязательный)
     * @return страница заказов пользователя
     */
    @GetMapping
    @Operation(
            summary = "Получить страницы с заказами пользователя",
            description = "Постраничная выборка заказов пользователя с фильтрами по статусу и диапазону дат.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Заказы получены",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = OrderDto.class)))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверные параметры запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
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
    public PageResponse<OrderDto> getAllOrders(@Parameter(hidden = true) @RequestHeader(USER_ID_HEADER) UUID userId,
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