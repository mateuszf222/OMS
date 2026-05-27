package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.PaymentDomainException;
import org.example.paymentservice.domain.model.PaymentTestData.PaymentIds;
import org.example.paymentservice.domain.model.payment.MaxAmountSpecification;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.paymentservice.domain.model.PaymentTestData.amountWithinPlnLimit;
import static org.example.paymentservice.domain.model.PaymentTestData.paymentIds;
import static org.example.paymentservice.domain.model.PaymentTestData.restoredPayment;
import static org.example.paymentservice.domain.model.PaymentTestData.standardPaymentAmount;

class PaymentTest {

    private final PaymentIds ids = paymentIds();

    @Test
    void shouldInitializePendingPayment() {
        Money amount = standardPaymentAmount();

        Payment payment = Payment.initialize(ids.orderId(), ids.customerId(), amount);

        PaymentAssert.assertThat(payment)
                .hasGeneratedId()
                .hasOrderId(ids.orderId())
                .belongsToCustomer(ids.customerId())
                .hasAmount(amount)
                .isPending();
    }

    @Test
    void shouldCompletePendingPayment() {
        Payment payment = Payment.initialize(ids.orderId(), ids.customerId(), standardPaymentAmount());

        payment.complete();

        PaymentAssert.assertThat(payment).isCompleted();
    }

    @Test
    void shouldFailPendingPayment() {
        Payment payment = Payment.initialize(ids.orderId(), ids.customerId(), standardPaymentAmount());

        payment.fail();

        PaymentAssert.assertThat(payment).isFailed();
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"COMPLETED", "FAILED"})
    void shouldRejectCompletingTerminalPayment(PaymentStatus terminalStatus) {
        Payment payment = restoredPayment(terminalStatus);

        assertThatExceptionOfType(PaymentDomainException.class)
                .isThrownBy(payment::complete)
                .withMessageContaining("Cannot complete payment in status: " + terminalStatus);

        PaymentAssert.assertThat(payment).hasStatus(terminalStatus);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"COMPLETED", "FAILED"})
    void shouldRejectFailingTerminalPayment(PaymentStatus terminalStatus) {
        Payment payment = restoredPayment(terminalStatus);

        assertThatExceptionOfType(PaymentDomainException.class)
                .isThrownBy(payment::fail)
                .withMessageContaining("Cannot fail payment in status: " + terminalStatus);

        PaymentAssert.assertThat(payment).hasStatus(terminalStatus);
    }

    @Test
    void shouldPassLimitValidationForValidAmount() {
        Payment payment = Payment.initialize(ids.orderId(), ids.customerId(), amountWithinPlnLimit());

        payment.checkSpecification(new MaxAmountSpecification());

        PaymentAssert.assertThat(payment).isPending();
    }
}
