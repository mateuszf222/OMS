package org.example.paymentservice.infrastructure.adapter.in.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MessageDeduplicationKeyTest {

    @Test
    void shouldRejectMissingKeyValue() {
        assertThatExceptionOfType(InvalidMessageDeduplicationKeyException.class)
                .isThrownBy(() -> new MessageDeduplicationKey(null))
                .withMessageContaining("cannot be null");
    }

    @Test
    void shouldRejectBlankKeyValue() {
        assertThatExceptionOfType(InvalidMessageDeduplicationKeyException.class)
                .isThrownBy(() -> new MessageDeduplicationKey(" "))
                .withMessageContaining("cannot be blank");
    }

    @Test
    void shouldRejectMissingFallbackEventIdWhenKafkaHeaderIsMissing() {
        OutboxEventId missingOutboxEventId = OutboxEventId.fromKafkaHeader(null);

        assertThatExceptionOfType(InvalidMessageDeduplicationKeyException.class)
                .isThrownBy(() -> missingOutboxEventId.valueOrFallback(null))
                .withMessageContaining("fallback event id");
    }
}
