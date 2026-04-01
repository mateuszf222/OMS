package org.example.orderservice.infrastructure.adapter.out.messaging;

import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxMessageRelay {

    private static final Logger log = LoggerFactory.getLogger("OutboxMessageRelay.java");
    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxMessageRelay(OutboxEventJpaRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Uruchamia się co 2 sekundy. Transakcja jest tu niezbędna do zatwierdzenia flagi processed = true
    // oraz utrzymania blokady pesymistycznej na czas przetwarzania.
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
                // Wysyłamy surowy string (JSON), ponieważ Outbox zapisał go już w tej formie.
                // Ustawiając topic na "order-events"
                kafkaTemplate.send("order-events", event.getAggregateId(), event.getPayload())
                        .get(); // Blokujemy asynchroniczność send() za pomocą .get(),
                // aby mieć gwarancję at-least-once (czekamy na ACK z Kafki)

                event.setProcessed(true);
                outboxRepository.save(event);
                log.debug("Pomyślnie wysłano zdarzenie o ID: {}", event.getId());

            } catch (Exception e) {
                // W przypadku błędu (np. broker Kafki leży), przerywamy pętlę.
                // Wyjątek spowoduje rollback transakcji DB, więc status 'processed' nie zostanie zapisany,
                // a zdarzenia zostaną przetworzone ponownie przy kolejnym cyklu.
                log.error("Błąd podczas wysyłania zdarzenia z Outboxa o ID: {}", event.getId(), e);
                throw new RuntimeException("Przerwano Relay z powodu awarii Kafki.", e);
            }
        }
    }
}