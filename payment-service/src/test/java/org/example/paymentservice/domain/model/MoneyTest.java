package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.InvalidPaymentAmountException;
import org.example.paymentservice.domain.exception.MissingPaymentCurrencyException;
import org.example.paymentservice.domain.exception.MoneyCurrencyMismatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.paymentservice.domain.model.MoneyBuilder.amount;
import static org.example.paymentservice.domain.model.MoneyBuilder.money;
import static org.example.paymentservice.domain.model.MoneyBuilder.plnMoney;
import static org.example.paymentservice.domain.model.PaymentTestData.PLN;
import static org.example.paymentservice.domain.model.PaymentTestData.moneyInDifferentCurrency;

class MoneyTest {

    @Test
    void shouldCreatePositiveMoneyValue() {
        Money money = plnMoney("123.45");

        MoneyAssert.assertThat(money)
                .hasValueOf("123.45 PLN");
    }

    @Test
    void shouldRejectMissingAmount() {
        assertThatExceptionOfType(InvalidPaymentAmountException.class)
                .isThrownBy(() -> money((BigDecimal) null, PLN))
                .withMessageContaining("cannot be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-0.01"})
    void shouldRejectNonPositiveAmount(String amount) {
        BigDecimal value = amount(amount);

        assertThatExceptionOfType(InvalidPaymentAmountException.class)
                .isThrownBy(() -> money(value, PLN))
                .withMessageContaining("greater than zero");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankCurrency(String currency) {
        assertThatExceptionOfType(MissingPaymentCurrencyException.class)
                .isThrownBy(() -> money("10.00", currency))
                .withMessageContaining("currency");
    }

    @Test
    void shouldConvertAmountToCentsForPaymentGatewayPayload() {
        Money money = plnMoney("10.99");

        MoneyAssert.assertThat(money)
                .convertsToCents(1099);
    }

    @Test
    void shouldCompareOnlyMoneyInSameCurrency() {
        Money lower = plnMoney("10.00");
        Money higher = plnMoney("10.01");

        MoneyAssert.assertThat(higher)
                .isGreaterThan(lower);
        assertThatExceptionOfType(MoneyCurrencyMismatchException.class)
                .isThrownBy(() -> lower.compareTo(moneyInDifferentCurrency()))
                .withMessageContaining("different currencies");
    }
}
