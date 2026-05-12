package org.example.orderservice.domain.event;

import org.example.orderservice.domain.model.Money;

import java.util.UUID;

public record OrderCreatedDomainEvent(
        UUID orderId,
        UUID customerId,
        Money totalAmount
) implements DomainEvent {
}