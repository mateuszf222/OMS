package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.PaymentDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final UUID orderId = UUID.randomUUID();

    @Test
    void shouldInitializePaymentSuccessfully() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("100.50"), "PLN");

        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualByComparingTo("100.50");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getId()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenInitializingWithZeroOrNegativeAmount() {
        assertThatThrownBy(() -> Payment.initialize(orderId, BigDecimal.ZERO, "PLN"))
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Invalid payment initialization parameters");

        assertThatThrownBy(() -> Payment.initialize(orderId, new BigDecimal("-10.00"), "PLN"))
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Invalid payment initialization parameters");
    }

    @Test
    void shouldCompletePayment() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("100.00"), "PLN");

        payment.complete();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void shouldFailPayment() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("100.00"), "PLN");

        payment.fail();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldThrowExceptionWhenCompletingAlreadyCompletedPayment() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("100.00"), "PLN");
        payment.complete();

        assertThatThrownBy(payment::complete)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Cannot complete payment in status");
    }

    @Test
    void shouldPassLimitValidationForValidAmount() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("9999.99"), "PLN");

        payment.validateLimits();
    }

    @Test
    void shouldThrowExceptionWhenAmountExceedsMaximumLimit() {
        Payment payment = Payment.initialize(orderId, new BigDecimal("10000.01"), "PLN");

        assertThatThrownBy(payment::validateLimits)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Insufficient funds: amount exceeds maximum limit");
    }
}