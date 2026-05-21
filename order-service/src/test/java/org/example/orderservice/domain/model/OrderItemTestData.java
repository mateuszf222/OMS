package org.example.orderservice.domain.model;

import static org.example.orderservice.domain.model.OrderItemBuilder.anOrderItem;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

final class OrderItemTestData {

    static final String EXPECTED_SUBTOTAL_FOR_THREE_ITEMS = "59.97";

    private OrderItemTestData() {
    }

    static OrderItem itemPricedAt1999WithQuantityThree() {
        return anOrderItem()
                .withQuantity(3)
                .withUnitPrice("19.99", PLN)
                .build();
    }

    static OrderItemBuilder itemWithQuantity(int quantity) {
        return anOrderItem()
                .withQuantity(quantity);
    }

    static OrderItemBuilder itemWithoutId() {
        return anOrderItem()
                .withId(null);
    }

    static OrderItemBuilder itemWithoutProductId() {
        return anOrderItem()
                .withProductId(null);
    }

    static OrderItemBuilder itemWithoutUnitPrice() {
        return anOrderItem()
                .withUnitPrice(null);
    }
}
