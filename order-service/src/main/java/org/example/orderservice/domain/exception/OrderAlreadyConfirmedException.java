package org.example.orderservice.domain.exception;

import org.example.orderservice.domain.model.OrderStatus;

public class OrderAlreadyConfirmedException extends InvalidOrderStateTransitionException {

    public OrderAlreadyConfirmedException() {
        super(OrderStatus.CONFIRMED, OrderStatus.CONFIRMED, "confirm payment");
    }
}
