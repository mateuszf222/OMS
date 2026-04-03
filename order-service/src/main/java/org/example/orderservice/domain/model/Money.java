package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "Kwota nie może być null.");
        Objects.requireNonNull(currency, "Waluta nie może być null.");

        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Mnożnik nie może być null.");
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderDomainException("Mnożnik nie może być ujemny.");
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
        Objects.requireNonNull(other, "Porównywana kwota nie może być null.");
        if (!this.currency.equals(other.currency())) {
            throw new OrderDomainException(
                    String.format("Niezgodność walut: oczekiwano %s, otrzymano %s", this.currency, other.currency())
            );
        }
    }
}