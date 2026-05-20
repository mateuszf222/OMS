package org.example.orderservice.domain.exception;

import java.util.Currency;

public class OrderItemsMustUseSameCurrencyException extends OrderDomainException {

    public OrderItemsMustUseSameCurrencyException(Currency expected, Currency actual) {
        super(String.format(
                "Produkty w zamówieniu muszą być w tej samej walucie. Oczekiwano %s, otrzymano %s.",
                expected,
                actual
        ));
    }
}
