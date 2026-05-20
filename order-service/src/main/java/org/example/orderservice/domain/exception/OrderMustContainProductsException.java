package org.example.orderservice.domain.exception;

public class OrderMustContainProductsException extends OrderDomainException {

    public OrderMustContainProductsException() {
        super("Zamówienie musi zawierać produkty.");
    }
}
