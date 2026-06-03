package org.example.paymentservice.domain.exception;

public sealed interface PaymentBusinessRefusal
        permits InvalidPaymentStateTransitionException, PaymentAmountLimitExceededException {
}
