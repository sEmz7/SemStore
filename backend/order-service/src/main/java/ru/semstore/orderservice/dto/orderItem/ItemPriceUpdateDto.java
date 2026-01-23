package ru.semstore.orderservice.dto.orderItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO для обновления цены товара")
public record ItemPriceUpdateDto(
        @Schema(description = "Цена товара", example = "1000.50")
        @NotNull
        BigDecimal price
) {
}
