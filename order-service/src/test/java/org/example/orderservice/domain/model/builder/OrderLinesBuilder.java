package org.example.orderservice.domain.model.builder;

import org.example.orderservice.domain.model.OrderLines;

import org.example.orderservice.domain.model.OrderItem;

import java.util.Arrays;
import java.util.List;

import static org.example.orderservice.domain.model.data.OrderTestData.standardOrderItem;

public class OrderLinesBuilder {

    private List<OrderItem> items = List.of(standardOrderItem());

    public static OrderLinesBuilder orderLines() {
        return new OrderLinesBuilder();
    }

    public OrderLinesBuilder withItems(OrderItem... items) {
        this.items = Arrays.asList(items);
        return this;
    }

    public OrderLinesBuilder withoutItems() {
        this.items = List.of();
        return this;
    }

    public OrderLines build() {
        return new OrderLines(items);
    }
}

