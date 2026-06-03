package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.MissingMoneyDataException;
import org.example.orderservice.domain.exception.MoneyCurrencyMismatchException;
import org.example.orderservice.domain.exception.NegativeMoneyMultiplierException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        if (amount == null) {
            throw new MissingMoneyDataException("amount");
        }
        if (currency == null) {
            throw new MissingMoneyDataException("currency");
        }

        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new MissingMoneyDataException("multiplier");
        }
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeMoneyMultiplierException(multiplier);
        }
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount()), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount()), this.currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount());
    }

    private void requireSameCurrency(Money other) {
        if (other == null) {
            throw new MissingMoneyDataException("other");
        }
        if (!this.currency.equals(other.currency())) {
            throw new MoneyCurrencyMismatchException(this.currency, other.currency());
        }
    }
}
