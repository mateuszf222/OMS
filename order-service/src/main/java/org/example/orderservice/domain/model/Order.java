package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public class Order {

    private final UUID id;
    private final UUID customerId;
    private OrderStatus status;
    private Money totalAmount;
    private final ZonedDateTime createdAt;
    private Long version; // Optimistic locking

    // Blokujemy automatyczny getter Lomboka, by chronić enkapsulację kolekcji
    @Getter(AccessLevel.NONE)
    private final List<OrderItem> items;

    // Prywatny konstruktor wymusza tworzenie obiektu przez metody fabrykujące
    private Order(UUID id, UUID customerId, OrderStatus status, List<OrderItem> items, ZonedDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = new ArrayList<>(items);
        this.createdAt = createdAt;
        this.totalAmount = calculateTotalAmount();
    }

    public static Order create(UUID customerId, List<OrderItem> items) {
        if (customerId == null) throw new OrderDomainException("CustomerId nie może być null.");
        if (items == null || items.isEmpty()) throw new OrderDomainException("Zamówienie musi zawierać produkty.");

        var firstCurrency = items.get(0).getUnitPrice().currency();
        boolean mixed = items.stream().anyMatch(i -> !i.getUnitPrice().currency().equals(firstCurrency));
        if (mixed) throw new OrderDomainException("Produkty w zamówieniu muszą być w tej samej walucie.");

        return new Order(UUID.randomUUID(), customerId, OrderStatus.PENDING, items, ZonedDateTime.now());
    }

    public static Order restore(UUID id, UUID customerId, OrderStatus status, List<OrderItem> items,
                                ZonedDateTime createdAt, Long version) {
        Order order = new Order(id, customerId, status, items, createdAt);
        order.version = version;
        return order;
    }

    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderDomainException("Nie można potwierdzić zamówienia w statusie: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new OrderDomainException("Nie można anulować zamówienia, które zostało już opłacone.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private Money calculateTotalAmount() {
        var currency = items.get(0).getUnitPrice().currency();
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(currency), Money::add);
    }

    // Własny getter dla bezpieczeństwa domeny
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}