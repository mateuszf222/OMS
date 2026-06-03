package org.example.paymentservice.domain.exception;

public final class MissingPaymentCurrencyException extends PaymentDomainException {

    public MissingPaymentCurrencyException() {
        super("Payment currency cannot be blank.");
    }
}
