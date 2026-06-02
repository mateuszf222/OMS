package org.example.notificationservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String CONSUMER_NAME = "notification-service";
    private static final String ORDER_CREATED_EVENT = "OrderCreatedEvent";
    private static final String PAYMENT_COMPLETED_EVENT = "PaymentCompletedEvent";
    private static final String PAYMENT_FAILED_EVENT = "PaymentFailedEvent";

    private final SendNotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    private final RedisMessageDeduplicator messageDeduplicator;

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void sendNotificationAfterOrderCreated(
            String payload,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        OrderCreatedEvent event = readNotificationEvent(
                payload,
                acknowledgment,
                ORDER_CREATED_EVENT,
                OrderCreatedEvent.class
        );
        sendNotificationIdempotently(event, payload, OutboxEventId.fromKafkaHeader(outboxEventIdHeader), acknowledgment);
    }

    @KafkaListener(topics = "payment-completed-events", groupId = "notification-service-group")
    public void sendNotificationAfterPaymentCompleted(
            String payload,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        PaymentCompletedEvent event = readNotificationEvent(
                payload,
                acknowledgment,
                PAYMENT_COMPLETED_EVENT,
                PaymentCompletedEvent.class
        );
        sendNotificationIdempotently(event, payload, OutboxEventId.fromKafkaHeader(outboxEventIdHeader), acknowledgment);
    }

    @KafkaListener(topics = "payment-failed-events", groupId = "notification-service-group")
    public void sendNotificationAfterPaymentFailed(
            String payload,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        PaymentFailedEvent event = readNotificationEvent(
                payload,
                acknowledgment,
                PAYMENT_FAILED_EVENT,
                PaymentFailedEvent.class
        );
        sendNotificationIdempotently(event, payload, OutboxEventId.fromKafkaHeader(outboxEventIdHeader), acknowledgment);
    }

    private void sendNotificationIdempotently(
            NotificationEvent event,
            String payload,
            OutboxEventId outboxEventId,
            Acknowledgment acknowledgment
    ) {
        if (event == null) {
            return;
        }

        MessageDeduplicationKey messageKey = MessageDeduplicationKey.forConsumedMessage(
                CONSUMER_NAME,
                event.eventType(),
                outboxEventId,
                event.notificationMessageId()
        );

        if (acknowledgeDuplicateNotificationMessage(messageKey, acknowledgment, event)) {
            return;
        }

        try {
            sendNotification(event);
            rememberNotificationMessageAndAcknowledge(messageKey, acknowledgment);
        } catch (RuntimeException e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            log.error("Failed to send notification after {} payload: {}", event.eventType(), payload, e);
            throw e;
        }
    }

    private void sendNotification(NotificationEvent event) {
        switch (event) {
            case OrderCreatedEvent orderCreated ->
                    notificationUseCase.sendOrderCreatedNotification(orderCreated.orderId(), orderCreated.customerId());
            case PaymentCompletedEvent paymentCompleted ->
                    notificationUseCase.sendPaymentSuccessNotification(
                            paymentCompleted.orderId(),
                            paymentCompleted.customerId()
                    );
            case PaymentFailedEvent paymentFailed ->
                    notificationUseCase.sendPaymentFailedNotification(
                            paymentFailed.orderId(),
                            paymentFailed.customerId(),
                            paymentFailed.reason()
                    );
        }
    }

    private <T extends NotificationEvent> T readNotificationEvent(
            String payload,
            Acknowledgment acknowledgment,
            String eventType,
            Class<T> eventClass
    ) {
        try {
            return objectMapper.readValue(payload, eventClass);
        } catch (JsonProcessingException ignored) {
            log.warn("Discarding malformed {} payload: {}", eventType, payload);
            acknowledgment.acknowledge();
            return null;
        }
    }

    private boolean acknowledgeDuplicateNotificationMessage(
            MessageDeduplicationKey messageKey,
            Acknowledgment acknowledgment,
            NotificationEvent event
    ) {
        if (messageDeduplicator.claimMessageForProcessing(messageKey)) {
            return false;
        }

        log.info("Duplicate {} skipped by notification-service.", event.eventType());
        acknowledgment.acknowledge();
        return true;
    }

    private void rememberNotificationMessageAndAcknowledge(
            MessageDeduplicationKey messageKey,
            Acknowledgment acknowledgment
    ) {
        messageDeduplicator.rememberMessageAsProcessed(messageKey);
        acknowledgment.acknowledge();
    }
}
