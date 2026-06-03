package org.example.paymentservice.application.exception;

public abstract sealed class PaymentApplicationException extends RuntimeException
        permits PaymentNotFoundException {

    protected PaymentApplicationException(String message) {
        super(message);
    }

    protected PaymentApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
