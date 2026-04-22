package org.example.paymentservice.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxMessageRelay {

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Map<String, String> EVENT_TOPIC_MAP = Map.of(
            "PaymentInitiatedEvent", "payment-initiated-events",
            "PaymentCompletedEvent", "payment-completed-events",
            "PaymentFailedEvent", "payment-failed-events"
    );

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventJpaEntity> unprocessedEvents = outboxRepository.findTop50UnprocessedEvents();

        if (unprocessedEvents.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed events in Outbox", unprocessedEvents.size());

        for (OutboxEventJpaEntity event : unprocessedEvents) {
            try {
                String topic = EVENT_TOPIC_MAP.get(event.getEventType());
                if (topic == null) {
                    log.error("Unknown event type: {}. Marking as processed.", event.getEventType());
                    event.setProcessed(true);
                    outboxRepository.save(event);
                    continue;
                }

                log.info("Relay - Sending to Kafka. Topic: {}, EventType: {}, AggregateId: {}",
                        topic, event.getEventType(), event.getAggregateId());

                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                        .get();

                event.setProcessed(true);
                outboxRepository.save(event);
                log.debug("Successfully published event ID: {}", event.getId());

            } catch (Exception e) {
                log.error("Failed to send event ID: {}. Will retry in next iteration.", event.getId(), e);
            }
        }
    }
}