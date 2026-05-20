package org.example.orderservice.domain.model;

import java.util.Arrays;
import java.util.Set;

public enum OrderStatus {
    DELIVERED,
    CANCELLED,
    RETURNED,
    SHIPPED(DELIVERED),
    CONFIRMED(SHIPPED, CANCELLED),
    PENDING(CONFIRMED, CANCELLED);

    private final Set<OrderStatus> allowed;

    OrderStatus(OrderStatus... allowed) {
        this.allowed = allowed.length == 0 ? Set.of() : Set.copyOf(Arrays.asList(allowed));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowed.contains(target);
    }

    public boolean isFinal() {
        return allowed.isEmpty();
    }
}
