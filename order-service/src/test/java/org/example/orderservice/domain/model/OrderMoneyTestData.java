package org.example.orderservice.domain.model;

import java.math.BigDecimal;

import static org.example.orderservice.domain.model.MoneyBuilder.money;
import static org.example.orderservice.domain.model.MoneyBuilder.multiplier;
import static org.example.orderservice.domain.model.OrderTestData.EUR;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

final class OrderMoneyTestData {

    static final String EXPECTED_SUM_OF_TWO_PLN_AMOUNTS = "150.75";
    static final String EXPECTED_TRIPLE_ITEM_PRICE = "30.00";

    private OrderMoneyTestData() {
    }

    static Money firstPlnAmountToAdd() {
        return money("100.50", PLN);
    }

    static Money secondPlnAmountToAdd() {
        return money("50.25", PLN);
    }

    static Money plnAmount() {
        return money("100.00", PLN);
    }

    static Money eurAmount() {
        return money("50.00", EUR);
    }

    static Money itemPriceToMultiply() {
        return money("10.00", PLN);
    }

    static BigDecimal invalidMultiplier(String value) {
        return multiplier(value);
    }
}
