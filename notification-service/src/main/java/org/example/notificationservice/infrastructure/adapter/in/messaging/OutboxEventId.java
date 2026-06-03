package org.example.notificationservice.infrastructure.adapter.in.messaging;

import java.nio.charset.StandardCharsets;

record OutboxEventId(String value) {

    OutboxEventId {
        value = value == null ? "" : value.trim();
    }

    static OutboxEventId fromKafkaHeader(byte[] headerValue) {
        if (headerValue == null || headerValue.length == 0) {
            return new OutboxEventId("");
        }

        return new OutboxEventId(new String(headerValue, StandardCharsets.UTF_8));
    }

    String valueOrFallback(String fallbackEventId) {
        if (!value.isBlank()) {
            return value;
        }

        if (fallbackEventId == null) {
            throw InvalidMessageDeduplicationKeyException.missingFallbackEventId();
        }

        return fallbackEventId;
    }
}
