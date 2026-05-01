package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import java.util.Collections;
import java.util.List;

public class OrderLines {
    private final List<OrderItem> items;

    public OrderLines(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderDomainException("Zamówienie musi zawierać produkty.");
        }
        var firstCurrency = items.get(0).getUnitPrice().currency();
        boolean mixed = items.stream().anyMatch(i -> !i.getUnitPrice().currency().equals(firstCurrency));

        if (mixed) {
            throw new OrderDomainException("Produkty w zamówieniu muszą być w tej samej walucie.");
        }
        this.items = List.copyOf(items);
    }

    public Money calculateTotal() {
        var currency = items.get(0).getUnitPrice().currency();
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(currency), Money::add);
    }

    public List<OrderItem> toList() {
        return Collections.unmodifiableList(items);
    }
}