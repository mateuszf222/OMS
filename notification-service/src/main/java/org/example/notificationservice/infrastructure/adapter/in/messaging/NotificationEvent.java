package org.example.notificationservice.infrastructure.adapter.in.messaging;

public sealed interface NotificationEvent permits OrderCreatedEvent, PaymentCompletedEvent, PaymentFailedEvent {

    String notificationMessageId();

    default String eventType() {
        return getClass().getSimpleName();
    }
}
