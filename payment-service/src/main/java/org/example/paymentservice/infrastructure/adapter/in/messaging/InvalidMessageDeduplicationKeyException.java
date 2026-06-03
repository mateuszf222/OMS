package org.example.paymentservice.infrastructure.adapter.in.messaging;

public final class InvalidMessageDeduplicationKeyException extends RuntimeException {

    private InvalidMessageDeduplicationKeyException(String message) {
        super(message);
    }

    static InvalidMessageDeduplicationKeyException missingKeyValue() {
        return new InvalidMessageDeduplicationKeyException("Message deduplication key value cannot be null.");
    }

    static InvalidMessageDeduplicationKeyException blankKeyValue() {
        return new InvalidMessageDeduplicationKeyException("Message deduplication key value cannot be blank.");
    }

    static InvalidMessageDeduplicationKeyException missingFallbackEventId() {
        return new InvalidMessageDeduplicationKeyException("Message deduplication fallback event id cannot be null.");
    }
}
