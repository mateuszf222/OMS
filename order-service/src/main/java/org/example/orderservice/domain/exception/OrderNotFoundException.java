package org.example.orderservice.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends OrderDomainException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}