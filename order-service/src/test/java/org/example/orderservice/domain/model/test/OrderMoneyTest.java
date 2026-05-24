package org.example.orderservice.domain.model.test;

import org.example.orderservice.domain.model.assertion.MoneyAssert;

import org.example.orderservice.domain.exception.MoneyCurrencyMismatchException;
import org.example.orderservice.domain.exception.NegativeMoneyMultiplierException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.EXPECTED_SUM_OF_TWO_PLN_AMOUNTS;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.EXPECTED_TRIPLE_ITEM_PRICE;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.eurAmount;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.firstPlnAmountToAdd;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.invalidMultiplier;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.itemPriceToMultiply;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.plnAmount;
import static org.example.orderservice.domain.model.data.OrderMoneyTestData.secondPlnAmountToAdd;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

class OrderMoneyTest {

    @Test
    void shouldAddMoneyWithSameCurrency() {
        MoneyAssert.assertThat(firstPlnAmountToAdd().add(secondPlnAmountToAdd()))
                .hasValue(EXPECTED_SUM_OF_TWO_PLN_AMOUNTS, PLN);
    }

    @Test
    void shouldRejectAddingDifferentCurrencies() {
        assertThatExceptionOfType(MoneyCurrencyMismatchException.class)
                .isThrownBy(() -> plnAmount().add(eurAmount()))
                .withMessageContaining("walut");
    }

    @Test
    void shouldMultiplyMoneyByPositiveInteger() {
        MoneyAssert.assertThat(itemPriceToMultiply().multiply(3))
                .hasValue(EXPECTED_TRIPLE_ITEM_PRICE, PLN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-2"})
    void shouldRejectNegativeMultiplier(String value) {
        assertThatExceptionOfType(NegativeMoneyMultiplierException.class)
                .isThrownBy(() -> itemPriceToMultiply().multiply(invalidMultiplier(value)))
                .withMessageContaining("ujemny");
    }
}

