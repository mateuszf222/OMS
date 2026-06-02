package org.example.notificationservice.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId,
        UUID customerId
) implements NotificationEvent {

    @Override
    public String notificationMessageId() {
        return paymentId.toString();
    }
}
