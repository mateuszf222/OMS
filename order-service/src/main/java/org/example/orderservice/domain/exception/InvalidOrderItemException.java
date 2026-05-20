package org.example.orderservice.domain.exception;

public class InvalidOrderItemException extends OrderDomainException {

    public InvalidOrderItemException(String message) {
        super(message);
    }
}
