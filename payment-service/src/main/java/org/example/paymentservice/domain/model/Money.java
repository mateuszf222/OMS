package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.InvalidPaymentAmountException;
import org.example.paymentservice.domain.exception.MissingPaymentCurrencyException;
import org.example.paymentservice.domain.exception.MissingPaymentDataException;
import org.example.paymentservice.domain.exception.MoneyCurrencyMismatchException;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) implements Comparable<Money> {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException(amount);
        }
        if (currency == null || currency.isBlank()) {
            throw new MissingPaymentCurrencyException();
        }
    }

    public int toCents() {
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    @Override
    public int compareTo(Money other) {
        if (other == null) {
            throw new MissingPaymentDataException("compared money");
        }
        if (!this.currency.equals(other.currency)) {
            throw new MoneyCurrencyMismatchException(this.currency, other.currency);
        }
        return this.amount.compareTo(other.amount);
    }

    public boolean isGreaterThan(Money other) {
        return this.compareTo(other) > 0;
    }
}
