package org.example.orderservice.domain.event;

import org.example.orderservice.domain.model.OrderStatus;

import java.util.UUID;

public record OrderCancelledDomainEvent(
        UUID orderId,
        UUID customerId,
        String reason,
        OrderStatus previousStatus
) implements DomainEvent {
}