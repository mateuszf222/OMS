package org.example.paymentservice.infrastructure.adapter.out.messaging;

import java.util.UUID;

public final class OutboxMessagePublishException extends RuntimeException {

    public OutboxMessagePublishException(UUID outboxEventId, Throwable cause) {
        super("Failed to publish payment outbox message: " + outboxEventId, cause);
    }
}
