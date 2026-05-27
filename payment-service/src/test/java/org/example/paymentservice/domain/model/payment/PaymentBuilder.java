package org.example.paymentservice.domain.model.payment;

import org.example.paymentservice.domain.model.Money;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PaymentBuilder {

    private UUID id = UUID.randomUUID();
    private UUID orderId = UUID.randomUUID();
    private UUID customerId = UUID.randomUUID();
    private Money amount = Money.of(new BigDecimal("100.00"), "PLN");
    private PaymentStatus status = PaymentStatus.PENDING;
    private ZonedDateTime createdAt = ZonedDateTime.now().minusMinutes(5);

    public static PaymentBuilder aPayment() {
        return new PaymentBuilder();
    }

    public PaymentBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public PaymentBuilder withOrderId(UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    public PaymentBuilder withCustomerId(UUID customerId) {
        this.customerId = customerId;
        return this;
    }

    public PaymentBuilder withAmount(String amount, String currency) {
        this.amount = Money.of(new BigDecimal(amount), currency);
        return this;
    }

    public PaymentBuilder withStatus(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public Payment build() {
        if (status == PaymentStatus.PENDING) {
            return Payment.initialize(orderId, customerId, amount);
        }

        return Payment.restore(new PaymentState(id, orderId, amount, status, createdAt, customerId));
    }
}
