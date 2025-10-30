package ru.semstore.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.dto.OrderUpdateDto;
import ru.semstore.orderservice.service.OrderService;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final String USER_ID_HEADER = "X-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@Valid @RequestBody OrderCreateDto dto,
                           @RequestHeader(USER_ID_HEADER) UUID userId) {
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
}
