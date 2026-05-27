package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.GatewayPaymentStatus;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.PaymentAssert;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.example.paymentservice.domain.model.PaymentTestData.pendingPayment;
import static org.example.paymentservice.domain.model.PaymentTestData.restoredPayment;
import static org.example.paymentservice.domain.model.PaymentTestData.unknownPaymentId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentWebhookService service;

    @ParameterizedTest(name = "{0} should move payment to {1}")
    @MethodSource("terminalGatewayStatuses")
    void shouldApplyTerminalGatewayStatus(GatewayPaymentStatus gatewayStatus, PaymentStatus expectedStatus) {
        Payment payment = pendingPayment();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.handleWebhook(payment.getId(), gatewayStatus);

        PaymentAssert.assertThat(payment).hasStatus(expectedStatus);
        verify(paymentRepository).save(payment);
    }

    @ParameterizedTest
    @EnumSource(value = GatewayPaymentStatus.class, names = {"PENDING", "UNKNOWN"})
    void shouldIgnoreNonTerminalGatewayStatus(GatewayPaymentStatus gatewayStatus) {
        Payment payment = pendingPayment();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.handleWebhook(payment.getId(), gatewayStatus);

        PaymentAssert.assertThat(payment).isPending();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"COMPLETED", "FAILED"})
    void shouldIgnoreDuplicateWebhookForTerminalPayment(PaymentStatus currentStatus) {
        Payment payment = restoredPayment(currentStatus);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.handleWebhook(payment.getId(), oppositeGatewayStatus(currentStatus));

        PaymentAssert.assertThat(payment).hasStatus(currentStatus);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldIgnoreWebhookForUnknownPayment() {
        var paymentId = unknownPaymentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        service.handleWebhook(paymentId, GatewayPaymentStatus.SUCCESS);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> terminalGatewayStatuses() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(GatewayPaymentStatus.SUCCESS, PaymentStatus.COMPLETED),
                org.junit.jupiter.params.provider.Arguments.of(GatewayPaymentStatus.FAILURE, PaymentStatus.FAILED)
        );
    }

    private static GatewayPaymentStatus oppositeGatewayStatus(PaymentStatus status) {
        return status == PaymentStatus.COMPLETED
                ? GatewayPaymentStatus.FAILURE
                : GatewayPaymentStatus.SUCCESS;
    }
}
