package org.example.orderservice.domain.exception;

public final class OrderLineCannotBeNullException extends OrderDomainException {

    public OrderLineCannotBeNullException() {
        super("Order line cannot be null.");
    }
}
