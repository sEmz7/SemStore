package ru.semstore.orderservice.dto.orderItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для создания товара")
public record OrderItemCreateDto(

        @Schema(description = "Ссылка на товар", example = "https://dw4.co/t/A/1tpEiZDfq",
                minLength = 2, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 2, max = 50)
        @NotNull
        String link,

        @Schema(description = "Размер товара", example = "44.5", minLength = 1, maxLength = 30,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 1, max = 30)
        @NotNull
        String size,

        @Schema(description = "Конфигурация/цвет товара", example = "Белые", minLength = 1, maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 1, max = 255)
        @NotNull
        String configuration
) {
}
