package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.PaymentDomainException;
import org.example.paymentservice.domain.specification.MaxAmountSpecification;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @Test
    void shouldInitializePaymentSuccessfully() {
        Money money = Money.of(new BigDecimal("100.50"), "PLN");

        Payment payment = Payment.initialize(orderId, customerId, money);

        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount().amount()).isEqualByComparingTo("100.50");
        assertThat(payment.getAmount().currency()).isEqualTo("PLN");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getId()).isNotNull();
    }

    @Test
    void shouldCompletePayment() {
        Payment payment = Payment.initialize(orderId, customerId,Money.of(new BigDecimal("100.00"), "PLN"));

        payment.complete();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void shouldFailPayment() {
        Payment payment = Payment.initialize(orderId, customerId,Money.of(new BigDecimal("100.00"), "PLN"));

        payment.fail();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldThrowExceptionWhenCompletingAlreadyCompletedPayment() {
        Payment payment = Payment.initialize(orderId, customerId,Money.of(new BigDecimal("100.00"), "PLN"));
        payment.complete();

        assertThatThrownBy(payment::complete)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Cannot complete payment in status");
    }

    @Test
    void shouldPassLimitValidationForValidAmount() {
        Payment payment = Payment.initialize(orderId, customerId,Money.of(new BigDecimal("9999.99"), "PLN"));

        payment.checkSpecification(new MaxAmountSpecification());
    }

//    @Test
//    void shouldThrowExceptionWhenAmountExceedsMaximumLimit() {
//        Payment payment = Payment.initialize(orderId, customerId,Money.of(new BigDecimal("10000.01"), "PLN"));
//
//        assertThatThrownBy(payment::checkSpecification)
//                .isInstanceOf(PaymentDomainException.class)
//                .hasMessageContaining("Amount exceeds maximum limit");
//    }
}