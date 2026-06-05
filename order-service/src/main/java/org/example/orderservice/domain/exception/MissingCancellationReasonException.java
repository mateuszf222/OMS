package org.example.orderservice.domain.exception;

public final class MissingCancellationReasonException extends OrderDomainException {

    public MissingCancellationReasonException() {
        super("Cancellation reason is required.");
    }
}
