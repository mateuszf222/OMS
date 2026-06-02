package org.example.orderservice.infrastructure.adapter.in.messaging;

import java.util.UUID;

public sealed interface PaymentEvent permits PaymentCompletedEvent, PaymentFailedEvent {

    UUID orderId();

    UUID paymentId();

    default String eventType() {
        return getClass().getSimpleName();
    }
}
