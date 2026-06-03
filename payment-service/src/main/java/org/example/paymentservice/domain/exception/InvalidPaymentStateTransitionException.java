package org.example.paymentservice.domain.exception;

import org.example.paymentservice.domain.model.payment.PaymentStatus;

public final class InvalidPaymentStateTransitionException extends RuntimeException implements PaymentBusinessRefusal {

    private final String operation;
    private final PaymentStatus currentStatus;
    private final PaymentStatus targetStatus;

    public InvalidPaymentStateTransitionException(
            String operation,
            PaymentStatus currentStatus,
            PaymentStatus targetStatus
    ) {
        super("Cannot " + operation + " payment in status " + currentStatus
                + ". Expected transition to: " + targetStatus + ".");
        this.operation = operation;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public String getOperation() {
        return operation;
    }

    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }

    public PaymentStatus getTargetStatus() {
        return targetStatus;
    }
}
