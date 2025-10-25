package ru.semstore.orderservice.dto.orderItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private UUID id;

    private String link;

    private String size;

    private String configuration;
}
