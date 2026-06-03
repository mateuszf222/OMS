package org.example.orderservice.domain.exception;

public final class OrderMustContainProductsException extends OrderDomainException {

    public OrderMustContainProductsException() {
        super("Zamówienie musi zawierać produkty.");
    }
}
