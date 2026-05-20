package org.example.orderservice.domain.exception;

import org.example.orderservice.domain.model.OrderStatus;

public class CannotCancelOrderException extends InvalidOrderStateTransitionException {

    public CannotCancelOrderException(OrderStatus currentStatus) {
        super(currentStatus, OrderStatus.CANCELLED, "cancel order");
    }
}
