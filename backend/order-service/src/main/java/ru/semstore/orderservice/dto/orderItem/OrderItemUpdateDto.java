package ru.semstore.orderservice.dto.orderItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для обновления товара")
public record OrderItemUpdateDto(
        @Schema(description = "Ссылка на товар", example = "https://dw4.co/t/A/1tpEiZDfq",
                minLength = 2, maxLength = 50)
        @Size(min = 2, max = 50)
        String link,

        @Schema(description = "Размер товара", example = "44.5", minLength = 1, maxLength = 30)
        @Size(min = 1, max = 30)
        String size,

        @Schema(description = "Конфигурация/цвет товара", example = "Белые", minLength = 1, maxLength = 255)
        @Size(min = 1, max = 255)
        String configuration
) {
}
