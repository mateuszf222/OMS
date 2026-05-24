package org.example.orderservice.domain.model.builder;

import org.example.orderservice.domain.model.OrderStatus;

import org.example.orderservice.domain.model.OrderState;

import org.example.orderservice.domain.model.OrderLines;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.example.orderservice.domain.model.data.OrderTestData.standardOrderLines;

public class OrderStateBuilder {
    private UUID id = UUID.randomUUID();
    private UUID customerId = UUID.randomUUID();
    private OrderStatus status = OrderStatus.CONFIRMED;
    private OrderLines lines = standardOrderLines();
    private ZonedDateTime createdAt = ZonedDateTime.now().minusDays(1);
    private Long version = 1L;

    public static OrderStateBuilder anOrderState() {
        return new OrderStateBuilder();
    }

    public OrderStateBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderStateBuilder withLines(OrderLines lines) {
        this.lines = lines;
        return this;
    }

    public OrderState build() {
        return new OrderState(id, customerId, status, lines, createdAt, version);
    }
}

