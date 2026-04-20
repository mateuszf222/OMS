package org.example.paymentservice.infrastructure.adapter.in.web;

public enum PayUStatus {
    NEW,
    PENDING,
    COMPLETED,
    CANCELED,
    REJECTED,
    UNKNOWN;

    public static PayUStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return UNKNOWN;
        }
        try {
            return PayUStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}