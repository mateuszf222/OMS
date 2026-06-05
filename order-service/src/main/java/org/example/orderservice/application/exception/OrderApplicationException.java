package org.example.orderservice.application.exception;

public abstract sealed class OrderApplicationException extends RuntimeException
        permits OrderNotFoundException, ProductNotAvailableException {

    public OrderApplicationException(String message) {
        super(message);
    }

    public OrderApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
