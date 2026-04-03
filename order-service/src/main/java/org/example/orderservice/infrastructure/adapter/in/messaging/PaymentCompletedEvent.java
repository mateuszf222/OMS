package org.example.orderservice.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(UUID orderId, UUID paymentId) {}