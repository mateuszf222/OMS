package org.example.paymentservice.infrastructure.adapter.out.messaging;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        UUID paymentId,
        String reason
) {}
