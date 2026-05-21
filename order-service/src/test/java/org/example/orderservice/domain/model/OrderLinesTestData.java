package org.example.orderservice.domain.model;

import static org.example.orderservice.domain.model.OrderTestData.emptyOrderLines;
import static org.example.orderservice.domain.model.OrderTestData.eurItem;
import static org.example.orderservice.domain.model.OrderTestData.orderLinesWith;
import static org.example.orderservice.domain.model.OrderTestData.plnItem;

final class OrderLinesTestData {

    static final String EXPECTED_TOTAL_FOR_TWO_PLN_LINES = "66.50";

    private OrderLinesTestData() {
    }

    static OrderLines twoPlnLines() {
        return orderLinesWith(
                plnItem("10.00", 2),
                plnItem("15.50", 3)
        );
    }

    static OrderLinesBuilder emptyLines() {
        return emptyOrderLines();
    }

    static OrderLines mixedCurrencyLines() {
        return orderLinesWith(
                plnItem("10.00", 1),
                eurItem("10.00", 1)
        );
    }

    static OrderLines singlePlnLine() {
        return orderLinesWith(plnItem("10.00", 1));
    }

    static OrderItem extraPlnLineForSnapshotMutation() {
        return plnItem("20.00", 1);
    }
}
