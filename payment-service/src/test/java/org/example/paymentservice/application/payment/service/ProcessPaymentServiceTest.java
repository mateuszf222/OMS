package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.exception.PaymentDomainException;
import org.example.paymentservice.domain.model.PaymentAssert;
import org.example.paymentservice.domain.model.payment.MaxAmountSpecification;
import org.example.paymentservice.domain.model.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.paymentservice.application.payment.service.PaymentCommandTestData.processPaymentAbovePlnLimit;
import static org.example.paymentservice.application.payment.service.PaymentCommandTestData.processPaymentWithinPlnLimit;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private ProcessPaymentService service;

    @BeforeEach
    void setUp() {
        service = new ProcessPaymentService(paymentRepository, new MaxAmountSpecification());
    }

    @Test
    void shouldInitializePendingPaymentAndSaveItWhenLimitSpecificationPasses() {
        ProcessPaymentCommand command = processPaymentWithinPlnLimit();

        service.processPayment(command);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        PaymentAssert.assertThat(paymentCaptor.getValue())
                .hasOrderId(command.orderId())
                .belongsToCustomer(command.customerId())
                .hasAmount(command.amount())
                .isPending();
    }

    @Test
    void shouldRejectPaymentAndNotSaveWhenLimitSpecificationFails() {
        assertThatExceptionOfType(PaymentDomainException.class)
                .isThrownBy(() -> service.processPayment(processPaymentAbovePlnLimit()))
                .withMessageContaining("maksymalny dopuszczalny limit");

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
