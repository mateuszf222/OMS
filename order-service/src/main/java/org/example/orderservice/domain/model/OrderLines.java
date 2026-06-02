package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;

import java.util.List;

public class OrderLines {
    private final List<OrderItem> items;

    public OrderLines(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderMustContainProductsException();
        }

        var firstCurrency = items.getFirst().getUnitPrice().currency();
        var itemWithDifferentCurrency = items.stream()
                .filter(item -> !item.getUnitPrice().currency().equals(firstCurrency))
                .findFirst();

        if (itemWithDifferentCurrency.isPresent()) {
            throw new OrderItemsMustUseSameCurrencyException(
                    firstCurrency,
                    itemWithDifferentCurrency.get().getUnitPrice().currency()
            );
        }
        this.items = List.copyOf(items);
    }

    public Money totalAmount() {
        var currency = items.getFirst().getUnitPrice().currency();
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(currency), Money::add);
    }

    public List<OrderItem> toList() {
        return items;
    }
}
