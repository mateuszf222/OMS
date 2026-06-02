package org.example.orderservice.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.example.orderservice.infrastructure.config.KafkaTopicsProperties;
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

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingOutboxMessages() {
        List<OutboxEventJpaEntity> pendingOutboxMessages = outboxRepository.findTop50PendingMessages();

        if (pendingOutboxMessages.isEmpty()) {
            return;
        }

        log.info("Found {} pending Order outbox messages.", pendingOutboxMessages.size());

        for (OutboxEventJpaEntity event : pendingOutboxMessages) {
            try {
                publishOutboxMessage(topics.getOrderEvents(), event);

                event.setProcessed(true);
                outboxRepository.save(event);

                log.debug("Published Order outbox event {}.", event.getId());
            } catch (Exception e) {
                log.error(
                        "Failed to publish Order outbox event {}. Event remains pending and will be retried.",
                        event.getId(),
                        e
                );
            }
        }
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
            throw new IllegalStateException("Interrupted while publishing outbox event.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish outbox event.", e);
        }
    }
}
