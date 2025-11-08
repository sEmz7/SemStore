package ru.semstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID userId;
    private UUID addressId;
}
