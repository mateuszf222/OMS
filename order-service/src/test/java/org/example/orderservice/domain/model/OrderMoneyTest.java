package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.MoneyCurrencyMismatchException;
import org.example.orderservice.domain.exception.NegativeMoneyMultiplierException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.orderservice.domain.model.OrderMoneyTestData.EXPECTED_SUM_OF_TWO_PLN_AMOUNTS;
import static org.example.orderservice.domain.model.OrderMoneyTestData.EXPECTED_TRIPLE_ITEM_PRICE;
import static org.example.orderservice.domain.model.OrderMoneyTestData.eurAmount;
import static org.example.orderservice.domain.model.OrderMoneyTestData.firstPlnAmountToAdd;
import static org.example.orderservice.domain.model.OrderMoneyTestData.invalidMultiplier;
import static org.example.orderservice.domain.model.OrderMoneyTestData.itemPriceToMultiply;
import static org.example.orderservice.domain.model.OrderMoneyTestData.plnAmount;
import static org.example.orderservice.domain.model.OrderMoneyTestData.secondPlnAmountToAdd;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

class OrderMoneyTest {

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money first = firstPlnAmountToAdd();
        Money second = secondPlnAmountToAdd();

        Money result = first.add(second);

        assertThat(result.amount()).isEqualByComparingTo(EXPECTED_SUM_OF_TWO_PLN_AMOUNTS);
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldRejectAddingDifferentCurrencies() {
        Money pln = plnAmount();
        Money eur = eurAmount();

        assertThatThrownBy(() -> pln.add(eur))
                .isInstanceOf(MoneyCurrencyMismatchException.class)
                .hasMessageContaining("walut");
    }

    @Test
    void shouldMultiplyMoneyByPositiveInteger() {
        Money money = itemPriceToMultiply();

        Money result = money.multiply(3);

        assertThat(result.amount()).isEqualByComparingTo(EXPECTED_TRIPLE_ITEM_PRICE);
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-2"})
    void shouldRejectNegativeMultiplier(String value) {
        Money money = itemPriceToMultiply();

        assertThatThrownBy(() -> money.multiply(invalidMultiplier(value)))
                .isInstanceOf(NegativeMoneyMultiplierException.class)
                .hasMessageContaining("ujemny");
    }
}
