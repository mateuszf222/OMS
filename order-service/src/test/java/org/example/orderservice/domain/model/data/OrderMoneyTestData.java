package org.example.orderservice.domain.model.data;

import org.example.orderservice.domain.model.Money;

import java.math.BigDecimal;

import static org.example.orderservice.domain.model.builder.MoneyBuilder.money;
import static org.example.orderservice.domain.model.builder.MoneyBuilder.multiplier;
import static org.example.orderservice.domain.model.data.OrderTestData.EUR;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

public final class OrderMoneyTestData {

    public static final String EXPECTED_SUM_OF_TWO_PLN_AMOUNTS = "150.75";
    public static final String EXPECTED_TRIPLE_ITEM_PRICE = "30.00";

    private OrderMoneyTestData() {
    }

    public static Money firstPlnAmountToAdd() {
        return money("100.50", PLN);
    }

    public static Money secondPlnAmountToAdd() {
        return money("50.25", PLN);
    }

    public static Money plnAmount() {
        return money("100.00", PLN);
    }

    public static Money eurAmount() {
        return money("50.00", EUR);
    }

    public static Money itemPriceToMultiply() {
        return money("10.00", PLN);
    }

    public static BigDecimal invalidMultiplier(String value) {
        return multiplier(value);
    }
}

