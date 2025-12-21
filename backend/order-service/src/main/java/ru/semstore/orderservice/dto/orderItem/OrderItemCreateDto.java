package ru.semstore.orderservice.dto.orderItem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderItemCreateDto(
        @Size(min = 2, max = 50)
        @NotNull
        String link,

        @Size(min = 1, max = 30)
        @NotNull
        String size,

        @Size(min = 1, max = 255)
        @NotNull
        String configuration
) {
}
