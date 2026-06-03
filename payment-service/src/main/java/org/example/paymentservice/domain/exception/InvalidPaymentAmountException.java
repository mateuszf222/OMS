package org.example.paymentservice.domain.exception;

import java.math.BigDecimal;

public final class InvalidPaymentAmountException extends PaymentDomainException {

    public InvalidPaymentAmountException(BigDecimal amount) {
        super(messageFor(amount));
    }

    private static String messageFor(BigDecimal amount) {
        if (amount == null) {
            return "Payment amount cannot be null.";
        }

        return "Payment amount must be greater than zero: " + amount;
    }
}
