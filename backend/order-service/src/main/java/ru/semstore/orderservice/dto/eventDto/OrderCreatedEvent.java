package ru.semstore.orderservice.dto.eventDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private UUID id;
    private UUID userId;
    private UUID addressId;
}
