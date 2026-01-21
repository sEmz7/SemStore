package ru.semstore.orderservice.controller.admin;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.dto.page.PageResponse;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.service.OrderService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Validated
public class AdminOrderController {
    private final OrderService orderService;

    /** Формат даты и времени для фильтрации заказов */
    private final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public PageResponse<OrderShortDto> getAllOrdersForCheck(
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd) {
        return orderService.getAllOrdersForCheck(page, size, status, rangeStart, rangeEnd);
    }

    @GetMapping("/{orderId}")
    public OrderFullDto getOrderById(@PathVariable("orderId") UUID orderId) {
        return orderService.getOrderByIdForAdmin(orderId);
    }
}
