package ru.semstore.common.dto;

public record VerificationCodeEvent(String email, String code) {
}
