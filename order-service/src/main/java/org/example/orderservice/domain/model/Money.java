package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderDomainException("Kwota nie może być ujemna lub null.");
        }
        if (currency == null) {
            throw new OrderDomainException("Waluta nie może być null.");
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) throw new OrderDomainException("Mnożnik nie może być ujemny.");
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency())) {
            throw new OrderDomainException("Nie można dodawać kwot w różnych walutach.");
        }
        return new Money(this.amount.add(other.amount()), this.currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}