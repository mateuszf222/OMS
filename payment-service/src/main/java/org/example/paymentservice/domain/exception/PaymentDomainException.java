package org.example.paymentservice.domain.exception;

public abstract sealed class PaymentDomainException extends RuntimeException
        permits InvalidPaymentAmountException,
        MissingPaymentCurrencyException,
        MissingPaymentDataException,
        MoneyCurrencyMismatchException {

    protected PaymentDomainException(String message) {
        super(message);
    }
}
