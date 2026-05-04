package org.example.paymentservice.domain.model;

import org.example.paymentservice.domain.exception.PaymentDomainException;
import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) implements Comparable<Money> {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentDomainException("Kwota płatności musi być większa od zera.");
        }
        if (currency == null || currency.isBlank()) {
            throw new PaymentDomainException("Waluta nie może być pusta.");
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
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot compare money with different currencies: "
                            + this.currency + " vs " + other.currency
            );
        }
        return this.amount.compareTo(other.amount);
    }

    public boolean isGreaterThan(Money other) {
        return this.compareTo(other) > 0;
    }
}