package org.example.orderservice.domain.model.data;

import org.example.orderservice.domain.model.builder.OrderItemBuilder;

import org.example.orderservice.domain.model.OrderItem;

import static org.example.orderservice.domain.model.builder.OrderItemBuilder.anOrderItem;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

public final class OrderItemTestData {

    public static final String EXPECTED_SUBTOTAL_FOR_THREE_ITEMS = "59.97";

    private OrderItemTestData() {
    }

    public static OrderItem itemPricedAt1999WithQuantityThree() {
        return anOrderItem()
                .withQuantity(3)
                .withUnitPrice("19.99", PLN)
                .build();
    }

    public static OrderItemBuilder itemWithQuantity(int quantity) {
        return anOrderItem()
                .withQuantity(quantity);
    }

    public static OrderItemBuilder itemWithoutId() {
        return anOrderItem()
                .withId(null);
    }

    public static OrderItemBuilder itemWithoutProductId() {
        return anOrderItem()
                .withProductId(null);
    }

    public static OrderItemBuilder itemWithoutUnitPrice() {
        return anOrderItem()
                .withUnitPrice(null);
    }
}

