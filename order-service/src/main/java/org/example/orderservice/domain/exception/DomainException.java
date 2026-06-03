package org.example.orderservice.domain.exception;

public abstract sealed class DomainException extends RuntimeException permits OrderDomainException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
