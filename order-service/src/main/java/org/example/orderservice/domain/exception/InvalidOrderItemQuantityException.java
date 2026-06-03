package org.example.orderservice.domain.exception;

public final class InvalidOrderItemQuantityException extends InvalidOrderItemException {

    public InvalidOrderItemQuantityException(int quantity) {
        super("Ilość produktów musi być większa niż 0. Otrzymano: " + quantity + ".");
    }
}
