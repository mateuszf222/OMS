package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private final Currency PLN = Currency.getInstance("PLN");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money m1 = new Money(new BigDecimal("100.50"), PLN);
        Money m2 = new Money(new BigDecimal("50.25"), PLN);

        Money result = m1.add(m2);

        assertThat(result.amount()).isEqualByComparingTo("150.75");
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money m1 = new Money(new BigDecimal("100.00"), PLN);
        Money m2 = new Money(new BigDecimal("50.00"), EUR);

        assertThatThrownBy(() -> m1.add(m2))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("Niezgodność walut");
    }

    @Test
    void shouldMultiplyMoney() {
        Money m1 = new Money(new BigDecimal("10.00"), PLN);

        Money result = m1.multiply(3);

        assertThat(result.amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldThrowExceptionForNegativeMultiplier() {
        Money m1 = new Money(new BigDecimal("10.00"), PLN);

        assertThatThrownBy(() -> m1.multiply(-2))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("Mnożnik nie może być ujemny");
    }
}