package org.example.orderservice.infrastructure.adapter.out.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.example.orderservice.infrastructure.config.KafkaTopicsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxMessageRelayTest {

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    @Mock
    private OutboxEventJpaRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties topics;

    @Test
    @SuppressWarnings("unchecked")
    void shouldPublishOutboxEventAndMarkItAsProcessed() {
        OutboxEventJpaEntity event = outboxEvent();
        when(outboxRepository.findTop50PendingMessages()).thenReturn(List.of(event));
        when(topics.getOrderEvents()).thenReturn(ORDER_EVENTS_TOPIC);
        stubKafkaSend();

        new OutboxMessageRelay(outboxRepository, kafkaTemplate, topics).publishPendingOutboxMessages();

        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());
        verify(outboxRepository).save(event);

        ProducerRecord<String, String> record = recordCaptor.getValue();
        assertThat(record.topic()).isEqualTo(ORDER_EVENTS_TOPIC);
        assertThat(record.key()).isEqualTo(event.getAggregateId());
        assertThat(record.value()).isEqualTo(event.getPayload());
        assertThat(headerValue(record, "outbox-event-id")).isEqualTo(event.getId().toString());
        assertThat(headerValue(record, "outbox-event-type")).isEqualTo(event.getEventType());
        assertThat(event.isProcessed()).isTrue();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubKafkaSend() {
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
    }

    private static String headerValue(ProducerRecord<String, String> record, String name) {
        return new String(record.headers().lastHeader(name).value(), StandardCharsets.UTF_8);
    }

    private static OutboxEventJpaEntity outboxEvent() {
        return OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("OrderCreatedDomainEvent")
                .payload("{\"event\":\"order-created\"}")
                .createdAt(ZonedDateTime.now())
                .processed(false)
                .build();
    }
}
