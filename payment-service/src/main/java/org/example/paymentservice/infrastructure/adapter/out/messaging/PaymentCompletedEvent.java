package org.example.paymentservice.infrastructure.adapter.out.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId
) {}
