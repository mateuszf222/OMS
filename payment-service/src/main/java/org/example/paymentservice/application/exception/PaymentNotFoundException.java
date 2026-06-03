package org.example.paymentservice.application.exception;

import java.util.UUID;

public final class PaymentNotFoundException extends PaymentApplicationException {

    private final UUID paymentId;

    public PaymentNotFoundException(UUID paymentId) {
        super("Payment not found: " + paymentId);
        this.paymentId = paymentId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }
}
