package ru.semstore.orderservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderUpdateDto(
        @NotNull
        UUID addressId
) {}