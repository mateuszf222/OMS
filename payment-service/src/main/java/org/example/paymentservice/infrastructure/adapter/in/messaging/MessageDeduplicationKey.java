package org.example.paymentservice.infrastructure.adapter.in.messaging;

public record MessageDeduplicationKey(String value) {

    public static final String OUTBOX_EVENT_ID_HEADER = "outbox-event-id";

    public MessageDeduplicationKey {
        if (value == null) {
            throw InvalidMessageDeduplicationKeyException.missingKeyValue();
        }

        value = value.trim();
        if (value.isBlank()) {
            throw InvalidMessageDeduplicationKeyException.blankKeyValue();
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
