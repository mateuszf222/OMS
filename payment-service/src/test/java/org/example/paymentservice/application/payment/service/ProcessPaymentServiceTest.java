package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.exception.PaymentDomainException;
import org.example.paymentservice.domain.model.PaymentAssert;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.specification.Specification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.paymentservice.application.payment.service.PaymentCommandTestData.processPaymentAbovePlnLimit;
import static org.example.paymentservice.application.payment.service.PaymentCommandTestData.processPaymentWithinPlnLimit;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Specification<Payment> maxAmountSpecification;

    @InjectMocks
    private ProcessPaymentService service;

    @Test
    void shouldInitializePendingPaymentAndSaveItWhenLimitSpecificationPasses() {
        ProcessPaymentCommand command = processPaymentWithinPlnLimit();
        when(maxAmountSpecification.isSatisfiedBy(any(Payment.class))).thenReturn(true);

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
        when(maxAmountSpecification.isSatisfiedBy(any(Payment.class))).thenReturn(false);
        when(maxAmountSpecification.getReasonNotSatisfied()).thenReturn("limit exceeded");

        assertThatExceptionOfType(PaymentDomainException.class)
                .isThrownBy(() -> service.processPayment(processPaymentAbovePlnLimit()))
                .withMessageContaining("limit exceeded");

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
