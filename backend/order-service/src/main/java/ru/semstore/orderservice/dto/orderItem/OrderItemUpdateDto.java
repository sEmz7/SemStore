package ru.semstore.orderservice.dto.orderItem;

import jakarta.validation.constraints.Size;

public record OrderItemUpdateDto(
        @Size(min = 2, max = 50)
        String link,

        @Size(min = 1, max = 30)
        String size,

        @Size(min = 1, max = 255)
        String configuration
) {
}
