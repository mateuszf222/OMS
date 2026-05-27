package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.model.payment.PaymentBuilder;
import org.example.paymentservice.domain.model.payment.PaymentStatus;

import java.util.UUID;

import static org.example.paymentservice.domain.model.MoneyBuilder.money;
import static org.example.paymentservice.domain.model.MoneyBuilder.plnMoney;

public final class PaymentTestData {

    public static final String PLN = "PLN";
    public static final String EUR = "EUR";

    private PaymentTestData() {
    }

    public static Money standardPaymentAmount() {
        return plnMoney("100.50");
    }

    public static Money amountWithinPlnLimit() {
        return plnMoney("9999.99");
    }

    public static Money amountAbovePlnLimit() {
        return plnMoney("10000.01");
    }

    public static PaymentIds paymentIds() {
        return new PaymentIds(UUID.randomUUID(), UUID.randomUUID());
    }

    public static UUID unknownPaymentId() {
        return UUID.randomUUID();
    }

    public static Payment pendingPayment() {
        return PaymentBuilder.aPayment().build();
    }

    public static Payment paymentWithAmount(String amount, String currency) {
        return PaymentBuilder.aPayment()
                .withAmount(amount, currency)
                .build();
    }

    public static Payment restoredPayment(PaymentStatus status) {
        return PaymentBuilder.aPayment()
                .withStatus(status)
                .build();
    }

    public static Money moneyInDifferentCurrency() {
        return money("10.00", EUR);
    }

    public record PaymentIds(UUID orderId, UUID customerId) {
    }
}
