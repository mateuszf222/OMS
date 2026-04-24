package org.example.paymentservice.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.model.PaymentStatus;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentCompletedEvent;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentFailedEvent;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentInitiatedEvent;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import org.example.paymentservice.infrastructure.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentEntityMapper mapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentStatus previousStatus = findPreviousStatus(payment.getId());
        boolean isNewPayment = previousStatus == null;

        PaymentJpaEntity entity = mapper.toJpaEntity(payment);
        PaymentJpaEntity savedEntity = jpaRepository.save(entity);

        if (isNewPayment && payment.getStatus() == PaymentStatus.PENDING) {
            publishPaymentInitiatedEvent(payment);
        } else if (previousStatus == PaymentStatus.PENDING && payment.getStatus() == PaymentStatus.COMPLETED) {
            publishPaymentCompletedEvent(payment);
        } else if (previousStatus == PaymentStatus.PENDING && payment.getStatus() == PaymentStatus.FAILED) {
            publishPaymentFailedEvent(payment);
        }

        return mapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomainModel);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDomainModel);
    }

    private PaymentStatus findPreviousStatus(UUID paymentId) {
        return jpaRepository.findById(paymentId)
                .map(PaymentJpaEntity::getStatus)
                .orElse(null);
    }

    private void publishPaymentInitiatedEvent(Payment payment) {
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        saveToOutbox("Payment", payment.getId().toString(), "PaymentInitiatedEvent", event);
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getOrderId(),
                payment.getId()
        );
        saveToOutbox("Payment", payment.getId().toString(), "PaymentCompletedEvent", event);
    }

    private void publishPaymentFailedEvent(Payment payment) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                payment.getOrderId(),
                payment.getId(),
                "Payment processing failed"
        );
        saveToOutbox("Payment", payment.getId().toString(), "PaymentFailedEvent", event);
    }

    private void saveToOutbox(String aggregateType, String aggregateId, String eventType, Object event) {
        try {
            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(ZonedDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Saved {} to Outbox for aggregate: {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize " + eventType, e);
        }
    }
}