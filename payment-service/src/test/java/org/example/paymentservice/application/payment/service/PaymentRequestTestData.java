package org.example.paymentservice.application.payment.service;

import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;

import static org.example.paymentservice.application.payment.service.RequestPaymentCommandBuilder.aPaymentRequest;
import static org.example.paymentservice.domain.model.PaymentTestData.amountAbovePlnLimit;

final class PaymentRequestTestData {

    private PaymentRequestTestData() {
    }

    static RequestPaymentCommand paymentRequestWithinPlnLimit() {
        return aPaymentRequest().build();
    }

    static RequestPaymentCommand paymentRequestAbovePlnLimit() {
        return aPaymentRequest()
                .withAmount(amountAbovePlnLimit())
                .build();
    }
}
