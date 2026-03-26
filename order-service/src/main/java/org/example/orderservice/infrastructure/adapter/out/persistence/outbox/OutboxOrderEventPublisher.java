package org.example.orderservice.infrastructure.adapter.out.persistence.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.application.port.out.OrderEventPublisher;
import org.example.orderservice.domain.exception.OrderDomainException;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.infrastructure.adapter.out.messaging.OrderCreatedEvent;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class OutboxOrderEventPublisher implements OrderEventPublisher {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxOrderEventPublisher(OutboxEventJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent eventPayload = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency().getCurrencyCode()
        );

        try {
            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("OrderCreatedEvent")
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .createdAt(ZonedDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Nie udało się zserializować zdarzenia Outbox.", e);
        }
    }
}