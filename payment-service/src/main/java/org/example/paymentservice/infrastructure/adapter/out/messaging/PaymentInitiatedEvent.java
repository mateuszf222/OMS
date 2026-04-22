package org.example.paymentservice.infrastructure.adapter.out.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiatedEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String currency
) {
}
