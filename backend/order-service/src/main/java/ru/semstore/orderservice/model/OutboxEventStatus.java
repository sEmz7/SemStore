package ru.semstore.orderservice.model;

public enum OutboxEventStatus {
    NEW,
    SENT,
    FAILED
}
