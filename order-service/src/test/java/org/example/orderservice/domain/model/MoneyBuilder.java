package org.example.orderservice.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

public final class MoneyBuilder {

    private MoneyBuilder() {
    }

    public static Money money(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static BigDecimal multiplier(String value) {
        return new BigDecimal(value);
    }
}
