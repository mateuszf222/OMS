package org.example.orderservice.domain.model.data;

import org.example.orderservice.domain.model.builder.OrderLinesBuilder;

import org.example.orderservice.domain.model.builder.OrderBuilder;

import org.example.orderservice.domain.model.OrderLines;

import org.example.orderservice.domain.model.OrderItem;

import java.util.Currency;

import static org.example.orderservice.domain.model.builder.OrderItemBuilder.anOrderItem;
import static org.example.orderservice.domain.model.builder.OrderLinesBuilder.orderLines;

public final class OrderTestData {

    public static final Currency PLN = Currency.getInstance("PLN");
    public static final Currency EUR = Currency.getInstance("EUR");

    private OrderTestData() {
    }

    public static OrderItem standardOrderItem() {
        return plnItem("100.00", 1);
    }

    public static OrderItem plnItem(String unitPrice, int quantity) {
        return item(unitPrice, PLN, quantity);
    }

    public static OrderItem eurItem(String unitPrice, int quantity) {
        return item(unitPrice, EUR, quantity);
    }

    public static OrderLines standardOrderLines() {
        return orderLinesWith(standardOrderItem());
    }

    public static OrderLines orderLinesWith(OrderItem... items) {
        return orderLines()
                .withItems(items)
                .build();
    }

    public static OrderLinesBuilder emptyOrderLines() {
        return orderLines().withoutItems();
    }

    public static OrderBuilder standardOrder() {
        return OrderBuilder.anOrder()
                .withItems(standardOrderItem());
    }

    private static OrderItem item(String unitPrice, Currency currency, int quantity) {
        return anOrderItem()
                .withQuantity(quantity)
                .withUnitPrice(unitPrice, currency)
                .build();
    }
}

