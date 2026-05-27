package org.example.paymentservice.domain.model;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;

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

    public MoneyAssert hasCurrency(String expected) {
        isNotNull();

        Assertions.assertThat(actual.currency())
                .as("money currency")
                .isEqualTo(expected);
        return this;
    }

    public MoneyAssert hasValue(String amount, String currency) {
        return hasAmount(amount)
                .hasCurrency(currency);
    }

    public MoneyAssert hasSameValueAs(Money expected) {
        isNotNull();
        Assertions.assertThat(expected)
                .as("expected money")
                .isNotNull();

        return hasAmount(expected.amount())
                .hasCurrency(expected.currency());
    }

    public MoneyAssert hasValueOf(String value) {
        String[] parts = value.strip().split(VALUE_SEPARATOR);
        if (parts.length != 2) {
            failWithMessage("Expected money value in '<amount> <currency>' format, but was <%s>", value);
        }

        return hasValue(parts[0], parts[1]);
    }

    public MoneyAssert convertsToCents(int expected) {
        isNotNull();

        Assertions.assertThat(actual.toCents())
                .as("money value in cents")
                .isEqualTo(expected);
        return this;
    }

    public MoneyAssert isGreaterThan(Money other) {
        isNotNull();

        Assertions.assertThat(actual.isGreaterThan(other))
                .as("money comparison")
                .isTrue();
        return this;
    }
}
