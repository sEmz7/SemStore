package ru.semstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCompletedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal price;
}
