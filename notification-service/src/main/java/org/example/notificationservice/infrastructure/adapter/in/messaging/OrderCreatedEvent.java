package org.example.notificationservice.infrastructure.adapter.in.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency
) implements NotificationEvent {

    @Override
    public String notificationMessageId() {
        return orderId.toString();
    }
}
