package org.example.orderservice.domain.model;

import java.util.Currency;
import java.util.UUID;

import static org.example.orderservice.domain.model.MoneyBuilder.money;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

public class OrderItemBuilder {

    private UUID id = UUID.randomUUID();
    private UUID productId = UUID.randomUUID();
    private int quantity = 1;
    private Money unitPrice = money("100.00", PLN);

    public static OrderItemBuilder anOrderItem() {
        return new OrderItemBuilder();
    }

    public OrderItemBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public OrderItemBuilder withProductId(UUID productId) {
        this.productId = productId;
        return this;
    }

    public OrderItemBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderItemBuilder withUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public OrderItemBuilder withUnitPrice(String amount, Currency currency) {
        return withUnitPrice(money(amount, currency));
    }

    public OrderItem build() {
        return new OrderItem(id, productId, quantity, unitPrice);
    }
}
