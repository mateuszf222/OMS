package org.example.orderservice.infrastructure.adapter.out.persistence;

public final class OutboxMessageSerializationException extends RuntimeException {

    public OutboxMessageSerializationException(Throwable cause) {
        super("Failed to serialize order outbox message.", cause);
    }
}
