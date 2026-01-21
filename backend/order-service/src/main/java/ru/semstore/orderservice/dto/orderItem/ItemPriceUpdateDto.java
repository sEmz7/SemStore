package ru.semstore.orderservice.dto.orderItem;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemPriceUpdateDto(
        @NotNull
        BigDecimal price
) {
}
