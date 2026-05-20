package org.example.orderservice.domain.exception;

import java.util.Currency;

public class MoneyCurrencyMismatchException extends InvalidMoneyOperationException {

    public MoneyCurrencyMismatchException(Currency expected, Currency actual) {
        super(String.format("Niezgodność walut: oczekiwano %s, otrzymano %s.", expected, actual));
    }
}
