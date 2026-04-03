package org.example.orderservice.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, UUID paymentId, String reason) {}
