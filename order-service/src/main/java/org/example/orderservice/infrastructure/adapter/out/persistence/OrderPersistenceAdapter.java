package org.example.orderservice.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.infrastructure.adapter.out.messaging.DomainToIntegrationEventTranslator;
import org.example.orderservice.infrastructure.adapter.out.messaging.IntegrationEvent;
import org.example.orderservice.infrastructure.adapter.out.messaging.OrderCreatedEvent;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.orderservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderEntityMapper mapper;
    private final DomainToIntegrationEventTranslator translator;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);

        publishDomainEvents(order);

        return mapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findByIdWithItems(id).map(mapper::toDomainModel);
    }

    private void publishDomainEvents(Order order) {
        for (DomainEvent domainEvent : order.pullDomainEvents()) {
            translator.translate(domainEvent)
                    .ifPresent(infraEvent -> saveToOutbox(order.getId(), domainEvent, infraEvent));
        }
    }

    private void saveToOutbox(UUID orderId, DomainEvent domainEvent, IntegrationEvent infraEvent) {
        try {
            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Order")
                    .aggregateId(orderId.toString())
                    .eventType(domainEvent.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(infraEvent))
                    .createdAt(ZonedDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}