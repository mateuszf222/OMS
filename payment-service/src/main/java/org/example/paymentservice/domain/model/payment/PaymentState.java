package org.example.paymentservice.domain.model.payment;

import org.example.paymentservice.domain.model.Money;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PaymentState(
        UUID id,
        UUID orderId,
        Money amount,
        PaymentStatus status,
        ZonedDateTime createdAt,
        UUID customerId
) {}