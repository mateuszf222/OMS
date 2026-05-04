package org.example.notificationservice.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        UUID paymentId,
        UUID customerId,
        String reason
) {}