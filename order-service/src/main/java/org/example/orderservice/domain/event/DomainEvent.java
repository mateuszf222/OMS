package org.example.orderservice.domain.event;

public sealed interface DomainEvent
        permits OrderCreatedDomainEvent {
}