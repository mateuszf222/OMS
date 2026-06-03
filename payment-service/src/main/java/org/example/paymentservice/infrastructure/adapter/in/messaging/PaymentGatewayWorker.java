package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.exception.PaymentNotFoundException;
import org.example.paymentservice.application.payment.port.out.PaymentGatewayOptions;
import org.example.paymentservice.application.payment.port.out.PaymentGatewayPort;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentInitiatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayWorker {

    private static final String CONSUMER_NAME = "payment-gateway-worker";
    private static final String PAYMENT_INITIATED_EVENT = "PaymentInitiatedEvent";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final ObjectMapper objectMapper;
    private final RedisMessageDeduplicator messageDeduplicator;

    @KafkaListener(topics = "payment-initiated-events", groupId = "payment-gateway-worker")
    public void initiateExternalPaymentAfterPaymentRequested(
            String payload,
            @Header(name = MessageDeduplicationKey.OUTBOX_EVENT_ID_HEADER, required = false) byte[] outboxEventIdHeader,
            Acknowledgment acknowledgment
    ) {
        PaymentInitiatedEvent event = readPaymentInitiatedEvent(payload, acknowledgment);
        if (event == null) {
            return;
        }

        log.info("Initiating external payment for paymentId: {}", event.paymentId());
        MessageDeduplicationKey messageKey = MessageDeduplicationKey.forConsumedMessage(
                CONSUMER_NAME,
                PAYMENT_INITIATED_EVENT,
                OutboxEventId.fromKafkaHeader(outboxEventIdHeader),
                event.paymentId().toString()
        );

        if (acknowledgeDuplicatePaymentInitiationMessage(messageKey, event, acknowledgment)) {
            return;
        }

        Payment payment = loadPaymentForGatewayInitiation(event, messageKey);

        if (!payment.isAwaitingGatewayDecision()) {
            log.info("Payment {} is already {}. Skipping gateway initiation.", event.paymentId(), payment.getStatus());
            rememberPaymentInitiationMessageAndAcknowledge(messageKey, acknowledgment);
            return;
        }

        try {
            String redirectUrl = paymentGatewayPort.initiatePayment(payment, PaymentGatewayOptions.standard("127.0.0.1"));
            log.info("Payment {} initiated successfully. Redirect URL: {}", event.paymentId(), redirectUrl);
            rememberPaymentInitiationMessageAndAcknowledge(messageKey, acknowledgment);
        } catch (RuntimeException e) {
            log.error("Failed to initiate external payment", e);
            rejectPaymentAfterGatewayFailure(event, messageKey, acknowledgment);
        }
    }

    private PaymentInitiatedEvent readPaymentInitiatedEvent(String payload, Acknowledgment acknowledgment) {
        try {
            return objectMapper.readValue(payload, PaymentInitiatedEvent.class);
        } catch (JsonProcessingException ignored) {
            log.warn("Discarding malformed PaymentInitiatedEvent payload: {}", payload);
            acknowledgment.acknowledge();
            return null;
        }
    }

    private boolean acknowledgeDuplicatePaymentInitiationMessage(
            MessageDeduplicationKey messageKey,
            PaymentInitiatedEvent event,
            Acknowledgment acknowledgment
    ) {
        if (messageDeduplicator.claimMessageForProcessing(messageKey)) {
            return false;
        }

        log.info("Duplicate PaymentInitiatedEvent skipped for paymentId: {}", event.paymentId());
        acknowledgment.acknowledge();
        return true;
    }

    private Payment loadPaymentForGatewayInitiation(
            PaymentInitiatedEvent event,
            MessageDeduplicationKey messageKey
    ) {
        try {
            return paymentRepository.findById(event.paymentId())
                    .orElseThrow(() -> new PaymentNotFoundException(event.paymentId()));
        } catch (RuntimeException e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            throw e;
        }
    }

    private void rejectPaymentAfterGatewayFailure(
            PaymentInitiatedEvent event,
            MessageDeduplicationKey messageKey,
            Acknowledgment acknowledgment
    ) {
        try {
            paymentRepository.findById(event.paymentId())
                    .filter(Payment::isAwaitingGatewayDecision)
                    .ifPresent(payment -> {
                        payment.fail();
                        paymentRepository.save(payment);
                    });
            rememberPaymentInitiationMessageAndAcknowledge(messageKey, acknowledgment);
        } catch (RuntimeException e) {
            messageDeduplicator.releaseMessageClaim(messageKey);
            log.error("Failed to reject payment after gateway failure", e);
            throw e;
        }
    }

    private void rememberPaymentInitiationMessageAndAcknowledge(
            MessageDeduplicationKey messageKey,
            Acknowledgment acknowledgment
    ) {
        messageDeduplicator.rememberMessageAsProcessed(messageKey);
        acknowledgment.acknowledge();
    }
}
