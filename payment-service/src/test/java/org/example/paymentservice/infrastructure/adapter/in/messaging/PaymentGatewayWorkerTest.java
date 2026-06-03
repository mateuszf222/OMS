package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.application.exception.PaymentNotFoundException;
import org.example.paymentservice.application.payment.port.out.PaymentGatewayOptions;
import org.example.paymentservice.application.payment.port.out.PaymentGatewayPort;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Money;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentState;
import org.example.paymentservice.domain.model.payment.PaymentStatus;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentInitiatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentGatewayWorkerTest {

    private PaymentRepository paymentRepository;
    private PaymentGatewayPort paymentGatewayPort;
    private RedisMessageDeduplicator messageDeduplicator;
    private Acknowledgment acknowledgment;
    private ObjectMapper objectMapper;
    private PaymentGatewayWorker worker;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentGatewayPort = mock(PaymentGatewayPort.class);
        messageDeduplicator = mock(RedisMessageDeduplicator.class);
        acknowledgment = mock(Acknowledgment.class);
        objectMapper = new ObjectMapper();
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(true);
        worker = new PaymentGatewayWorker(paymentRepository, paymentGatewayPort, objectMapper, messageDeduplicator);
    }

    @Test
    void shouldInitiatePendingPaymentOnceAndAcknowledge() throws Exception {
        Payment payment = payment(PaymentStatus.PENDING);
        PaymentInitiatedEvent event = eventFor(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentGatewayPort.initiatePayment(any(Payment.class), any(PaymentGatewayOptions.class)))
                .thenReturn("https://payu.example/redirect");

        worker.initiateExternalPaymentAfterPaymentRequested(payload(event), null, acknowledgment);

        verify(paymentGatewayPort).initiatePayment(any(Payment.class), any(PaymentGatewayOptions.class));
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSkipDuplicatePaymentInitiatedEventBeforeGatewayCall() throws Exception {
        Payment payment = payment(PaymentStatus.PENDING);
        PaymentInitiatedEvent event = eventFor(payment);
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(false);

        worker.initiateExternalPaymentAfterPaymentRequested(payload(event), null, acknowledgment);

        verifyNoInteractions(paymentRepository, paymentGatewayPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMalformedPaymentInitiatedEventWithoutGatewayProcessing() {
        worker.initiateExternalPaymentAfterPaymentRequested("{malformed-json", null, acknowledgment);

        verifyNoInteractions(paymentRepository, paymentGatewayPort, messageDeduplicator);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldTreatTerminalPaymentAsBusinessIdempotencyGuard() throws Exception {
        Payment payment = payment(PaymentStatus.COMPLETED);
        PaymentInitiatedEvent event = eventFor(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        worker.initiateExternalPaymentAfterPaymentRequested(payload(event), null, acknowledgment);

        verify(paymentGatewayPort, never()).initiatePayment(any(Payment.class), any(PaymentGatewayOptions.class));
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldReleaseClaimAndRethrowWhenPaymentIsMissingForGatewayInitiation() throws Exception {
        Payment payment = payment(PaymentStatus.PENDING);
        PaymentInitiatedEvent event = eventFor(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(PaymentNotFoundException.class)
                .isThrownBy(() -> worker.initiateExternalPaymentAfterPaymentRequested(payload(event), null, acknowledgment))
                .satisfies(exception -> org.assertj.core.api.Assertions.assertThat(exception.getPaymentId())
                        .isEqualTo(payment.getId()));

        verify(messageDeduplicator).releaseMessageClaim(any(MessageDeduplicationKey.class));
        verify(paymentGatewayPort, never()).initiatePayment(any(Payment.class), any(PaymentGatewayOptions.class));
        verify(acknowledgment, never()).acknowledge();
    }

    private String payload(PaymentInitiatedEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }

    private static PaymentInitiatedEvent eventFor(Payment payment) {
        return new PaymentInitiatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount().amount(),
                payment.getAmount().currency()
        );
    }

    private static Payment payment(PaymentStatus status) {
        return Payment.restore(new PaymentState(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("249.99"), "PLN"),
                status,
                ZonedDateTime.now(),
                UUID.randomUUID()
        ));
    }
}
