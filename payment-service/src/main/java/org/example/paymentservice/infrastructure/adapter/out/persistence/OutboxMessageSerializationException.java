package org.example.paymentservice.infrastructure.adapter.out.persistence;

public final class OutboxMessageSerializationException extends RuntimeException {

    public OutboxMessageSerializationException(String eventType, Throwable cause) {
        super("Failed to serialize payment outbox message: " + eventType, cause);
    }
}
