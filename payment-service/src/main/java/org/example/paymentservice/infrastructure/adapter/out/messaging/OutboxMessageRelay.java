package org.example.paymentservice.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.example.paymentservice.infrastructure.config.kafka.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxMessageRelay {

    private static final String OUTBOX_EVENT_ID_HEADER = "outbox-event-id";
    private static final String OUTBOX_EVENT_TYPE_HEADER = "outbox-event-type";

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingOutboxMessages() {
        List<OutboxEventJpaEntity> pendingOutboxMessages = outboxRepository.findTop50PendingMessages();

        for (OutboxEventJpaEntity event : pendingOutboxMessages) {
            String topic = topicFor(event.getEventType());

            if (topic == null || topic.isBlank()) {
                log.error(
                        "Missing Kafka topic for event type {}. Outbox event {} remains pending.",
                        event.getEventType(),
                        event.getId()
                );
                continue;
            }

            try {
                publishOutboxMessage(topic, event);

                event.setProcessed(true);
                outboxRepository.save(event);

                log.debug("Published Payment outbox event {} to topic {}.", event.getId(), topic);
            } catch (Exception e) {
                log.error(
                        "Failed to publish Payment outbox event {}. Event remains pending and will be retried.",
                        event.getId(),
                        e
                );
            }
        }
    }

    private String topicFor(String eventType) {
        return switch (eventType) {
            case "PaymentInitiatedEvent" -> topics.getPaymentInitiatedEvents();
            case "PaymentCompletedEvent" -> topics.getPaymentCompletedEvents();
            case "PaymentFailedEvent" -> topics.getPaymentFailedEvents();
            default -> null;
        };
    }

    private void publishOutboxMessage(String topic, OutboxEventJpaEntity event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                event.getAggregateId(),
                event.getPayload()
        );
        record.headers().add(OUTBOX_EVENT_ID_HEADER, event.getId().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(OUTBOX_EVENT_TYPE_HEADER, event.getEventType().getBytes(StandardCharsets.UTF_8));

        try {
            kafkaTemplate.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OutboxMessagePublishException(event.getId(), e);
        } catch (Exception e) {
            throw new OutboxMessagePublishException(event.getId(), e);
        }
    }
}
