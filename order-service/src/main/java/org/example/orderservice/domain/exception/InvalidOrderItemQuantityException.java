package org.example.orderservice.domain.exception;

public class InvalidOrderItemQuantityException extends InvalidOrderItemException {

    public InvalidOrderItemQuantityException(int quantity) {
        super("Ilość produktów musi być większa niż 0. Otrzymano: " + quantity + ".");
    }
}
