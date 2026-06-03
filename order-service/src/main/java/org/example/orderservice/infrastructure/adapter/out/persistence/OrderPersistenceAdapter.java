package org.example.orderservice.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.infrastructure.adapter.out.messaging.DomainToIntegrationEventTranslator;
import org.example.orderservice.infrastructure.adapter.out.messaging.IntegrationEvent;
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
    private final OrderEntityMapper entityMapper;
    private final DomainToIntegrationEventTranslator integrationEventTranslator;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = entityMapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);

        recordDomainEventsInOutbox(order);

        return entityMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findByIdWithItems(id).map(entityMapper::toDomainModel);
    }

    private void recordDomainEventsInOutbox(Order order) {
        for (DomainEvent domainEvent : order.pullDomainEvents()) {
            integrationEventTranslator.translate(domainEvent)
                    .ifPresent(integrationEvent -> appendOrderMessageToOutbox(order.getId(), domainEvent, integrationEvent));
        }
    }

    private void appendOrderMessageToOutbox(UUID orderId, DomainEvent domainEvent, IntegrationEvent integrationEvent) {
        try {
            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Order")
                    .aggregateId(orderId.toString())
                    .eventType(domainEvent.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(integrationEvent))
                    .createdAt(ZonedDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new OutboxMessageSerializationException(e);
        }
    }
}
