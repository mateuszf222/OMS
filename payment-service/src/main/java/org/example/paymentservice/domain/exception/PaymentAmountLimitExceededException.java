package org.example.paymentservice.domain.exception;

public final class PaymentAmountLimitExceededException extends RuntimeException implements PaymentBusinessRefusal {

    public PaymentAmountLimitExceededException(String reason) {
        super(reason);
    }
}
