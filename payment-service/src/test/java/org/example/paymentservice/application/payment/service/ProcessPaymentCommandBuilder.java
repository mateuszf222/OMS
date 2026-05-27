package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.ProcessPaymentCommand;
import org.example.paymentservice.domain.model.Money;

import java.util.UUID;

import static org.example.paymentservice.domain.model.PaymentTestData.amountWithinPlnLimit;

class ProcessPaymentCommandBuilder {

    private UUID orderId = UUID.randomUUID();
    private UUID customerId = UUID.randomUUID();
    private Money amount = amountWithinPlnLimit();

    static ProcessPaymentCommandBuilder aProcessPaymentCommand() {
        return new ProcessPaymentCommandBuilder();
    }

    ProcessPaymentCommandBuilder withAmount(Money amount) {
        this.amount = amount;
        return this;
    }

    ProcessPaymentCommand build() {
        return new ProcessPaymentCommand(orderId, customerId, amount);
    }
}
