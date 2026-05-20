package org.example.orderservice.domain.exception;

import org.example.orderservice.domain.model.OrderStatus;

public class CannotConfirmPaymentException extends InvalidOrderStateTransitionException {

    public CannotConfirmPaymentException(OrderStatus currentStatus) {
        super(currentStatus, OrderStatus.CONFIRMED, "confirm payment");
    }
}
