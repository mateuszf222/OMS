package org.example.paymentservice.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentStatus;
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
    private final PaymentEntityMapper entityMapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        Optional<PaymentStatus> previousStatusOpt = findPreviousStatus(payment.getId());

        PaymentJpaEntity entity = entityMapper.toJpaEntity(payment);
        PaymentJpaEntity savedEntity = jpaRepository.save(entity);

        if (previousStatusOpt.isEmpty() && payment.getStatus() == PaymentStatus.PENDING) {
            recordPaymentInitiatedInOutbox(payment);
        } else if (previousStatusOpt.isPresent() && previousStatusOpt.get() == PaymentStatus.PENDING) {
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                recordPaymentCompletedInOutbox(payment);
            } else if (payment.getStatus() == PaymentStatus.FAILED) {
                recordPaymentFailedInOutbox(payment);
            }
        }

        return entityMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id).map(entityMapper::toDomainModel);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).map(entityMapper::toDomainModel);
    }

    private Optional<PaymentStatus> findPreviousStatus(UUID paymentId) {
        return jpaRepository.findById(paymentId)
                .map(PaymentJpaEntity::getStatus);
    }

    private void recordPaymentInitiatedInOutbox(Payment payment) {
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount().amount(),
                payment.getAmount().currency()
        );
        appendPaymentMessageToOutbox("Payment", payment.getId().toString(), "PaymentInitiatedEvent", event);
    }

    private void recordPaymentCompletedInOutbox(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getCustomerId()
        );
        appendPaymentMessageToOutbox("Payment", payment.getId().toString(), "PaymentCompletedEvent", event);
    }

    private void recordPaymentFailedInOutbox(Payment payment) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getCustomerId(),
                "Payment rejected by gateway"
        );
        appendPaymentMessageToOutbox("Payment", payment.getId().toString(), "PaymentFailedEvent", event);
    }

    private void appendPaymentMessageToOutbox(String aggregateType, String aggregateId, String eventType, Object event) {
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
            log.info("Recorded {} in Outbox for aggregate: {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            throw new OutboxMessageSerializationException(eventType, e);
        }
    }
}
