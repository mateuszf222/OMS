package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;
import org.example.paymentservice.application.payment.port.in.RequestPaymentUseCase;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Money;
import org.example.paymentservice.domain.model.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderEventListenerTest {

    private RequestPaymentUseCase requestPaymentUseCase;
    private PaymentRepository paymentRepository;
    private OrderEventMapper orderEventMapper;
    private RedisMessageDeduplicator messageDeduplicator;
    private Acknowledgment acknowledgment;
    private ObjectMapper objectMapper;
    private OrderEventListener listener;

    @BeforeEach
    void setUp() {
        requestPaymentUseCase = mock(RequestPaymentUseCase.class);
        paymentRepository = mock(PaymentRepository.class);
        orderEventMapper = mock(OrderEventMapper.class);
        messageDeduplicator = mock(RedisMessageDeduplicator.class);
        acknowledgment = mock(Acknowledgment.class);
        objectMapper = new ObjectMapper();
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(true);
        listener = new OrderEventListener(
                requestPaymentUseCase,
                paymentRepository,
                objectMapper,
                orderEventMapper,
                messageDeduplicator
        );
    }

    @Test
    void shouldRequestPaymentAfterOrderCreatedEventAndAcknowledge() throws Exception {
        OrderCreatedEvent event = orderCreatedEvent();
        RequestPaymentCommand command = commandFor(event);
        when(paymentRepository.findByOrderId(event.orderId())).thenReturn(Optional.empty());
        when(orderEventMapper.toPaymentRequest(event)).thenReturn(command);

        listener.requestPaymentAfterOrderCreated(payload(event), null, acknowledgment);

        verify(requestPaymentUseCase).requestPayment(command);
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSkipDuplicateOrderCreatedEventBeforePaymentLookup() throws Exception {
        OrderCreatedEvent event = orderCreatedEvent();
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(false);

        listener.requestPaymentAfterOrderCreated(payload(event), null, acknowledgment);

        verifyNoInteractions(paymentRepository, requestPaymentUseCase, orderEventMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMalformedOrderCreatedEventWithoutBusinessProcessing() {
        listener.requestPaymentAfterOrderCreated("{malformed-json", null, acknowledgment);

        verifyNoInteractions(paymentRepository, requestPaymentUseCase, orderEventMapper, messageDeduplicator);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldTreatExistingPaymentAsBusinessIdempotencyGuard() throws Exception {
        OrderCreatedEvent event = orderCreatedEvent();
        when(paymentRepository.findByOrderId(event.orderId())).thenReturn(Optional.of(mock(Payment.class)));

        listener.requestPaymentAfterOrderCreated(payload(event), null, acknowledgment);

        verify(requestPaymentUseCase, never()).requestPayment(any());
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    private String payload(OrderCreatedEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }

    private static OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("249.99"),
                "PLN"
        );
    }

    private static RequestPaymentCommand commandFor(OrderCreatedEvent event) {
        return new RequestPaymentCommand(
                event.orderId(),
                event.customerId(),
                Money.of(event.totalAmount(), event.currency())
        );
    }
}
