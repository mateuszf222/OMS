package org.example.orderservice.domain.exception;

import org.example.orderservice.domain.model.OrderStatus;

public class CannotConfirmCancelledOrderException extends InvalidOrderStateTransitionException {

    public CannotConfirmCancelledOrderException() {
        super(OrderStatus.CANCELLED, OrderStatus.CONFIRMED, "confirm payment");
    }
}
