package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;
import org.example.paymentservice.application.payment.port.in.RequestPaymentUseCase;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final String CONSUMER_NAME = "payment-service";
    private static final String ORDER_CREATED_EVENT = "OrderCreatedEvent";

    private final RequestPaymentUseCase requestPaymentUseCase;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final OrderEventMapper orderEventMapper;
    private final RedisMessageDeduplicator messageDeduplicator;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.orderEvents}", groupId = "#{@kafkaTopicsProperties.groups.paymentService}")
    public void requestPaymentAfterOrderCreated(
            String payload,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        log.info("Received OrderCreatedEvent: {}", payload);

        OrderCreatedEvent event = readOrderCreatedEvent(payload, acknowledgment);
        if (event == null) {
            return;
        }

        MessageDeduplicationKey messageKey = MessageDeduplicationKey.forConsumedMessage(
                CONSUMER_NAME,
                ORDER_CREATED_EVENT,
                OutboxEventId.fromKafkaHeader(outboxEventIdHeader),
                event.orderId().toString()
        );

        if (acknowledgeDuplicateOrderCreatedMessage(messageKey, event, acknowledgment)) {
            return;
        }

        try {
            requestPaymentForOrder(event);
            rememberOrderCreatedMessageAndAcknowledge(messageKey, acknowledgment);
        } catch (RuntimeException e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            log.error("Failed to request payment after OrderCreatedEvent", e);
            throw e;
        }
    }

    private OrderCreatedEvent readOrderCreatedEvent(String payload, Acknowledgment acknowledgment) {
        try {
            return objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (JsonProcessingException ignored) {
            log.warn("Discarding malformed OrderCreatedEvent payload: {}", payload);
            acknowledgment.acknowledge();
            return null;
        }
    }

    private boolean acknowledgeDuplicateOrderCreatedMessage(
            MessageDeduplicationKey messageKey,
            OrderCreatedEvent event,
            Acknowledgment acknowledgment
    ) {
        if (messageDeduplicator.claimMessageForProcessing(messageKey)) {
            return false;
        }

        log.info("Duplicate OrderCreatedEvent skipped for orderId: {}", event.orderId());
        acknowledgment.acknowledge();
        return true;
    }

    private void requestPaymentForOrder(OrderCreatedEvent event) {
        if (paymentRepository.findByOrderId(event.orderId()).isPresent()) {
            log.info("Payment for orderId {} already exists. Skipping duplicate event.", event.orderId());
            return;
        }

        RequestPaymentCommand command = orderEventMapper.toPaymentRequest(event);
        requestPaymentUseCase.requestPayment(command);
        log.info("Payment requested for orderId: {}", event.orderId());
    }

    private void rememberOrderCreatedMessageAndAcknowledge(
            MessageDeduplicationKey messageKey,
            Acknowledgment acknowledgment
    ) {
        messageDeduplicator.rememberMessageAsProcessed(messageKey);
        acknowledgment.acknowledge();
    }
}
