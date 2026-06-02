package org.example.paymentservice.infrastructure.adapter.in.messaging;

import java.util.Objects;

public record MessageDeduplicationKey(String value) {

    public static final String OUTBOX_EVENT_ID_HEADER = "outbox-event-id";

    public MessageDeduplicationKey {
        value = Objects.requireNonNull(value, "deduplication key value cannot be null").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("deduplication key value cannot be blank");
        }
    }

    static MessageDeduplicationKey forConsumedMessage(
            String consumerName,
            String eventType,
            OutboxEventId outboxEventId,
            String fallbackEventId
    ) {
        String eventId = outboxEventId.valueOrFallback(fallbackEventId);
        return new MessageDeduplicationKey("kafka:dedup:%s:%s:%s".formatted(consumerName, eventType, eventId));
    }

    String asRedisKey() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
