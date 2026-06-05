package org.example.orderservice.domain.cancellation;

public record CustomerCancellationReason(String value) implements CancellationReason {

    public CustomerCancellationReason {
        value = CancellationReason.requireNonBlank(value);
    }
}
