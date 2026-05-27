package org.example.paymentservice.domain.model;

import java.math.BigDecimal;

public final class MoneyBuilder {

    private MoneyBuilder() {
    }

    public static Money money(String amount, String currency) {
        return money(amount(amount), currency);
    }

    public static Money money(BigDecimal amount, String currency) {
        return Money.of(amount, currency);
    }

    public static Money plnMoney(String amount) {
        return money(amount, PaymentTestData.PLN);
    }

    public static Money eurMoney(String amount) {
        return money(amount, PaymentTestData.EUR);
    }

    public static BigDecimal amount(String amount) {
        return new BigDecimal(amount);
    }
}
