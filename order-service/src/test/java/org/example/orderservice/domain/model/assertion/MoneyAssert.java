package org.example.orderservice.domain.model.assertion;

import org.example.orderservice.domain.model.Money;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.util.Currency;

public class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

    private static final String VALUE_SEPARATOR = "\\s+";

    private MoneyAssert(Money actual) {
        super(actual, MoneyAssert.class);
    }

    public static MoneyAssert assertThat(Money actual) {
        return new MoneyAssert(actual);
    }

    public MoneyAssert hasAmount(BigDecimal expected) {
        isNotNull();

        Assertions.assertThat(actual.amount())
                .as("money amount")
                .isEqualByComparingTo(expected);
        return this;
    }

    public MoneyAssert hasAmount(String expected) {
        return hasAmount(new BigDecimal(expected));
    }

    public MoneyAssert hasCurrency(Currency expected) {
        isNotNull();

        Assertions.assertThat(actual.currency())
                .as("money currency")
                .isEqualTo(expected);
        return this;
    }

    public MoneyAssert hasCurrency(String expected) {
        return hasCurrency(Currency.getInstance(expected));
    }

    public MoneyAssert hasValue(BigDecimal amount, Currency currency) {
        return hasAmount(amount)
                .hasCurrency(currency);
    }

    public MoneyAssert hasValue(String amount, Currency currency) {
        return hasValue(new BigDecimal(amount), currency);
    }

    public MoneyAssert hasValue(String amount, String currency) {
        return hasValue(new BigDecimal(amount), Currency.getInstance(currency));
    }

    public MoneyAssert hasSameValueAs(Money expected) {
        isNotNull();
        Assertions.assertThat(expected)
                .as("expected money")
                .isNotNull();

        return hasValue(expected.amount(), expected.currency());
    }

    public MoneyAssert hasValueOf(String value) {
        String[] parts = value.strip().split(VALUE_SEPARATOR);
        if (parts.length != 2) {
            failWithMessage("Expected money value in '<amount> <currency>' format, but was <%s>", value);
        }

        return hasValue(parts[0], parts[1]);
    }
}

