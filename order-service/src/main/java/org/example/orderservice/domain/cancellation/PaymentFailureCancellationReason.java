package org.example.orderservice.domain.cancellation;

public record PaymentFailureCancellationReason(String value) implements CancellationReason {

    public PaymentFailureCancellationReason {
        value = CancellationReason.requireNonBlank(value);
    }
}
