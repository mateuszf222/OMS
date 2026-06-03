package org.example.orderservice.domain.exception;

public abstract sealed class InvalidOrderItemException extends OrderDomainException permits
        InvalidOrderItemQuantityException,
        MissingOrderItemDataException {

    public InvalidOrderItemException(String message) {
        super(message);
    }
}
