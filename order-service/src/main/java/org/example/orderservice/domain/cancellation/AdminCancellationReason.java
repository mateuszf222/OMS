package org.example.orderservice.domain.cancellation;

public record AdminCancellationReason(String value) implements CancellationReason {

    public AdminCancellationReason {
        value = CancellationReason.requireNonBlank(value);
    }
}
