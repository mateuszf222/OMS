package org.example.paymentservice.domain.exception;

public final class MissingPaymentDataException extends PaymentDomainException {

    public MissingPaymentDataException(String fieldName) {
        super("Payment " + fieldName + " cannot be null.");
    }
}
