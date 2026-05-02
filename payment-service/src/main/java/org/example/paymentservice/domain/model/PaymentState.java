package org.example.paymentservice.domain.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PaymentState(
        UUID id,
        UUID orderId,
        Money amount,
        PaymentStatus status,
        ZonedDateTime createdAt
) {}