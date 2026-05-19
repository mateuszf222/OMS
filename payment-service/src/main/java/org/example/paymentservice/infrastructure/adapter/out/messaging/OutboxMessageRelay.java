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

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventJpaEntity> unprocessedEvents = outboxRepository.findTop50UnprocessedEvents();

        for (OutboxEventJpaEntity event : unprocessedEvents) {
            String topic = EVENT_TOPIC_MAP.get(event.getEventType());

            if (topic == null || topic.isBlank()) {
                log.error("CRITICAL: Brak skonfigurowanego tematu Kafka dla eventu typu: {}. " +
                        "Event ID: {} pozostaje nieprzetworzony!", event.getEventType(), event.getId());
                continue;
            }

            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get(); // .get() wymusza synchroniczne potwierdzenie (blokuje do momentu sukcesu)

                event.setProcessed(true);
                outboxRepository.save(event);

                log.debug("Pomyślnie wysłano event {} na temat {}", event.getId(), topic);

            } catch (Exception e) {
                log.error("Błąd podczas wysyłania eventu {} na Kafkę. " +
                        "Event pozostaje nieprzetworzony i zostanie ponowiony.", event.getId(), e);
            }
        }
    }
}