package org.example.orderservice.domain.exception;

public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) { super(message); }
    public OrderDomainException(String message, Throwable cause) { super(message, cause); }
}