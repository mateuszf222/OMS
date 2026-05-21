package org.example.orderservice.domain.model;

import java.util.List;
import java.util.UUID;

import static org.example.orderservice.domain.model.OrderTestData.standardOrderItem;

public class OrderBuilder {
    private UUID customerId = UUID.randomUUID();
    private List<OrderItem> items = List.of(standardOrderItem());

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withCustomerId(UUID customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderBuilder withItems(OrderItem... items) {
        this.items = List.of(items);
        return this;
    }

    public OrderBuilder withoutItems() {
        this.items = List.of();
        return this;
    }

    public Order build() {
        return Order.create(customerId, items);
    }
}
