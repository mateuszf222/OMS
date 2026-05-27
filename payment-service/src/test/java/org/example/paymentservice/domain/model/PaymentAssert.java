package org.example.paymentservice.domain.model;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentStatus;

import java.util.UUID;

public class PaymentAssert extends AbstractAssert<PaymentAssert, Payment> {

    private PaymentAssert(Payment actual) {
        super(actual, PaymentAssert.class);
    }

    public static PaymentAssert assertThat(Payment actual) {
        return new PaymentAssert(actual);
    }

    public PaymentAssert hasGeneratedId() {
        isNotNull();

        Assertions.assertThat(actual.getId())
                .as("payment id")
                .isNotNull();
        return this;
    }

    public PaymentAssert hasOrderId(UUID expected) {
        isNotNull();

        Assertions.assertThat(actual.getOrderId())
                .as("payment order id")
                .isEqualTo(expected);
        return this;
    }

    public PaymentAssert belongsToCustomer(UUID expected) {
        isNotNull();

        Assertions.assertThat(actual.getCustomerId())
                .as("payment customer id")
                .isEqualTo(expected);
        return this;
    }

    public PaymentAssert hasAmount(Money expected) {
        isNotNull();

        MoneyAssert.assertThat(actual.getAmount())
                .hasSameValueAs(expected);
        return this;
    }

    public PaymentAssert hasStatus(PaymentStatus expected) {
        isNotNull();

        Assertions.assertThat(actual.getStatus())
                .as("payment status")
                .isEqualTo(expected);
        return this;
    }

    public PaymentAssert isPending() {
        return hasStatus(PaymentStatus.PENDING);
    }

    public PaymentAssert isCompleted() {
        return hasStatus(PaymentStatus.COMPLETED);
    }

    public PaymentAssert isFailed() {
        return hasStatus(PaymentStatus.FAILED);
    }
}
