package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.exception.PaymentAmountLimitExceededException;
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
import static org.example.paymentservice.application.payment.service.PaymentRequestTestData.paymentRequestAbovePlnLimit;
import static org.example.paymentservice.application.payment.service.PaymentRequestTestData.paymentRequestWithinPlnLimit;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private RequestPaymentService service;

    @BeforeEach
    void setUp() {
        service = new RequestPaymentService(paymentRepository, new MaxAmountSpecification());
    }

    @Test
    void shouldCreatePendingPaymentRequestWhenAmountLimitAllowsIt() {
        RequestPaymentCommand command = paymentRequestWithinPlnLimit();

        service.requestPayment(command);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        PaymentAssert.assertThat(paymentCaptor.getValue())
                .hasOrderId(command.orderId())
                .belongsToCustomer(command.customerId())
                .hasAmount(command.amount())
                .isPending();
    }

    @Test
    void shouldRejectPaymentRequestAboveAmountLimit() {
        assertThatExceptionOfType(PaymentAmountLimitExceededException.class)
                .isThrownBy(() -> service.requestPayment(paymentRequestAbovePlnLimit()))
                .withMessageContaining("maksymalny dopuszczalny limit");

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
