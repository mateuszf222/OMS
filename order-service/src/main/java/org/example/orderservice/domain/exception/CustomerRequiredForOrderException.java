package org.example.orderservice.domain.exception;

public final class CustomerRequiredForOrderException extends OrderDomainException {

    public CustomerRequiredForOrderException() {
        super("CustomerId nie może być null.");
    }
}
