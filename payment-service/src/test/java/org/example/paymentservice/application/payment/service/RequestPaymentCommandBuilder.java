package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;
import org.example.paymentservice.domain.model.Money;

import java.util.UUID;

import static org.example.paymentservice.domain.model.PaymentTestData.amountWithinPlnLimit;

class RequestPaymentCommandBuilder {

    private UUID orderId = UUID.randomUUID();
    private UUID customerId = UUID.randomUUID();
    private Money amount = amountWithinPlnLimit();

    static RequestPaymentCommandBuilder aPaymentRequest() {
        return new RequestPaymentCommandBuilder();
    }

    RequestPaymentCommandBuilder withAmount(Money amount) {
        this.amount = amount;
        return this;
    }

    RequestPaymentCommand build() {
        return new RequestPaymentCommand(orderId, customerId, amount);
    }
}
