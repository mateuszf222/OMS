package org.example.orderservice.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.example.orderservice.infrastructure.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxMessageRelay {

    private static final Logger log = LoggerFactory.getLogger("OutboxMessageRelay.java");
    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topics;


    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventJpaEntity> unprocessedEvents = outboxRepository.findTop50UnprocessedEvents();

        if (unprocessedEvents.isEmpty()) {
            return;
        }

        log.info("Znaleziono {} nieprzetworzonych zdarzeń w Outbox.", unprocessedEvents.size());

        for (OutboxEventJpaEntity event : unprocessedEvents) {
            try {
                log.info("Outbox Relay - Wysyłanie na Kafkę. ID: {}, Payload: {}", event.getId(), event.getPayload());
                kafkaTemplate.send(topics.getOrderEvents(), event.getAggregateId(), event.getPayload())
                        .get();

                event.setProcessed(true);
                outboxRepository.save(event);
                log.debug("Pomyślnie wysłano zdarzenie o ID: {}", event.getId());

            } catch (Exception e) {
                log.error("Błąd podczas wysyłania zdarzenia z Outboxa o ID: {}", event.getId(), e);
                throw new RuntimeException("Przerwano Relay z powodu awarii Kafki.", e);
            }
        }
    }
}