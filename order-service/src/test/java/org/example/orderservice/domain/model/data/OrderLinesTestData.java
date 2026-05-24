package org.example.orderservice.domain.model.data;

import org.example.orderservice.domain.model.builder.OrderLinesBuilder;

import org.example.orderservice.domain.model.OrderLines;

import org.example.orderservice.domain.model.OrderItem;

import static org.example.orderservice.domain.model.data.OrderTestData.emptyOrderLines;
import static org.example.orderservice.domain.model.data.OrderTestData.eurItem;
import static org.example.orderservice.domain.model.data.OrderTestData.orderLinesWith;
import static org.example.orderservice.domain.model.data.OrderTestData.plnItem;

public final class OrderLinesTestData {

    public static final String EXPECTED_TOTAL_FOR_TWO_PLN_LINES = "66.50";

    private OrderLinesTestData() {
    }

    public static OrderLines twoPlnLines() {
        return orderLinesWith(
                plnItem("10.00", 2),
                plnItem("15.50", 3)
        );
    }

    public static OrderLinesBuilder emptyLines() {
        return emptyOrderLines();
    }

    public static OrderLines mixedCurrencyLines() {
        return orderLinesWith(
                plnItem("10.00", 1),
                eurItem("10.00", 1)
        );
    }

    public static OrderLines singlePlnLine() {
        return orderLinesWith(plnItem("10.00", 1));
    }

    public static OrderItem extraPlnLineForSnapshotMutation() {
        return plnItem("20.00", 1);
    }
}

