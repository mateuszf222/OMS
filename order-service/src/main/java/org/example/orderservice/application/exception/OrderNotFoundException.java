package org.example.orderservice.application.exception;

import java.util.UUID;

public final class OrderNotFoundException extends OrderApplicationException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
