package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.ProcessPaymentCommand;

import static org.example.paymentservice.application.payment.service.ProcessPaymentCommandBuilder.aProcessPaymentCommand;
import static org.example.paymentservice.domain.model.PaymentTestData.amountAbovePlnLimit;

final class PaymentCommandTestData {

    private PaymentCommandTestData() {
    }

    static ProcessPaymentCommand processPaymentWithinPlnLimit() {
        return aProcessPaymentCommand().build();
    }

    static ProcessPaymentCommand processPaymentAbovePlnLimit() {
        return aProcessPaymentCommand()
                .withAmount(amountAbovePlnLimit())
                .build();
    }
}
