package ru.semstore.orderservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.OrderUpdateDto;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public OrderDto create(@Valid @RequestBody OrderCreateDto dto, @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.create(dto, userId);
    }

    @PatchMapping("/{orderId}")
    public OrderDto update(@Valid @RequestBody OrderUpdateDto dto, @PathVariable("orderId") UUID orderId) {
        return orderService.update(dto, orderId);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("orderId") UUID orderId, @RequestHeader(USER_ID_HEADER) UUID userId) {
        orderService.delete(orderId, userId);
    }

    @GetMapping("/{orderId}")
    public OrderDto getById(@PathVariable("orderId") UUID orderId, @RequestHeader(USER_ID_HEADER) UUID userId) {
        return orderService.getById(orderId, userId);
    }

    @GetMapping
    public List<OrderDto> getAllOrders(@RequestHeader(USER_ID_HEADER) UUID userId,
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
