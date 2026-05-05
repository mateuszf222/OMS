package org.example.orderservice.infrastructure.adapter.out.messaging;

import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID customerId,
        String reason
) implements IntegrationEvent {
}