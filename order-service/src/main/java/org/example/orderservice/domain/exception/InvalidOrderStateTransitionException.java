package org.example.orderservice.domain.exception;

import org.example.orderservice.domain.model.OrderStatus;

public class InvalidOrderStateTransitionException extends OrderDomainException {

    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public InvalidOrderStateTransitionException(OrderStatus currentStatus, OrderStatus targetStatus, String action) {
        super(String.format(
                "Nie można wykonać operacji '%s' dla zamówienia w statusie %s. Oczekiwane przejście do: %s.",
                action,
                currentStatus,
                targetStatus
        ));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
}
