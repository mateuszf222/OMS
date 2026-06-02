package org.example.orderservice.infrastructure.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderUseCase;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentUseCase;
import org.example.orderservice.domain.exception.OrderDomainException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventListener {

    private static final String CONSUMER_NAME = "order-service";

    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final RedisMessageDeduplicator messageDeduplicator;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentCompletedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void completeOrderPaymentAfterPaymentCompleted(
            PaymentCompletedEvent event,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        applyPaymentEventToOrder(event, OutboxEventId.fromKafkaHeader(outboxEventIdHeader), acknowledgment);
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentFailedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void cancelOrderAfterPaymentFailed(
            PaymentFailedEvent event,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        applyPaymentEventToOrder(event, OutboxEventId.fromKafkaHeader(outboxEventIdHeader), acknowledgment);
    }

    private void applyPaymentEventToOrder(
            PaymentEvent event,
            OutboxEventId outboxEventId,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} for orderId: {}", event.getClass().getSimpleName(), event.orderId());
        handleIdempotently(
                event,
                outboxEventId,
                acknowledgment,
                this::applyPaymentOutcomeToOrder
        );
    }

    private void applyPaymentOutcomeToOrder(PaymentEvent event) {
        switch (event) {
            case PaymentCompletedEvent completed -> completeOrderPayment(completed);
            case PaymentFailedEvent failed -> cancelOrderBecausePaymentFailed(failed);
        }
    }

    private void completeOrderPayment(PaymentCompletedEvent event) {
        completePaymentUseCase.completePayment(new CompletePaymentCommand(event.orderId(), event.paymentId()));
        log.info("Order {} payment completed successfully", event.orderId());
    }

    private void cancelOrderBecausePaymentFailed(PaymentFailedEvent event) {
        cancelOrderUseCase.cancelOrder(new CancelOrderCommand(event.orderId(), event.reason()));
        log.info("Order {} cancelled due to failed payment: {}", event.orderId(), event.reason());
    }

    private void handleIdempotently(
            PaymentEvent event,
            OutboxEventId outboxEventId,
            Acknowledgment acknowledgment,
            Consumer<PaymentEvent> businessProcessor
    ) {
        String paymentId = event.paymentId().toString();
        MessageDeduplicationKey messageKey = MessageDeduplicationKey.forConsumedMessage(
                CONSUMER_NAME,
                event.eventType(),
                outboxEventId,
                paymentId
        );

        if (!messageDeduplicator.claimMessageForProcessing(messageKey)) {
            log.info("Duplicate {} skipped for paymentId: {}", event.eventType(), paymentId);
            acknowledgment.acknowledge();
            return;
        }

        try {
            businessProcessor.accept(event);
            messageDeduplicator.rememberMessageAsProcessed(messageKey);
            acknowledgment.acknowledge();

        } catch (OrderDomainException e) {
            messageDeduplicator.rememberMessageAsProcessed(messageKey);
            log.warn("Idempotent skip for {} with paymentId {}: {}", event.eventType(), paymentId, e.getMessage());
            acknowledgment.acknowledge();

        } catch (OptimisticLockingFailureException e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            log.warn(
                    "Concurrent modification detected while applying {} with paymentId {}. Event will be redelivered.",
                    event.eventType(),
                    paymentId
            );

        } catch (Exception e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            log.error("Unexpected error applying {} with paymentId {}", event.eventType(), paymentId, e);
            throw e;
        }
    }
}
