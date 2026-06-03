package org.example.paymentservice.infrastructure.adapter.out.payu;

public final class PaymentGatewayCommunicationException extends RuntimeException {

    public PaymentGatewayCommunicationException(String message) {
        super(message);
    }

    public PaymentGatewayCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
