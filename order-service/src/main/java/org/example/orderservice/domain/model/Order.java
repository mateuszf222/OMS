package org.example.orderservice.domain.model;

import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCancelledDomainEvent;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.example.orderservice.domain.exception.OrderDomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public class Order {

    private final UUID id;
    private final UUID customerId;
    private OrderStatus status;
    private final ZonedDateTime createdAt;
    private Long version;

    private final OrderLines items;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Order(UUID id, UUID customerId, OrderStatus status, List<OrderItem> items, ZonedDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = new OrderLines(items);
        this.createdAt = createdAt;
    }

    public static Order create(UUID customerId, List<OrderItem> items) {
        if (customerId == null) throw new OrderDomainException("CustomerId nie może być null.");
        if (items == null || items.isEmpty()) throw new OrderDomainException("Zamówienie musi zawierać produkty.");

        var firstCurrency = items.get(0).getUnitPrice().currency();
        boolean mixed = items.stream().anyMatch(i -> !i.getUnitPrice().currency().equals(firstCurrency));
        if (mixed) throw new OrderDomainException("Produkty w zamówieniu muszą być w tej samej walucie.");

        Order order = new Order(UUID.randomUUID(), customerId, OrderStatus.PENDING, items, ZonedDateTime.now());

        order.domainEvents.add(new OrderCreatedDomainEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount()
        ));

        return order;
    }

    public static Order restore(OrderState state) {
        Order order = new Order(
                state.id(),
                state.customerId(),
                state.status(),
                state.lines().toList(), 
                state.createdAt()
        );
        order.version = state.version();
        return order;
    }

    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderDomainException("Nie można potwierdzić zamówienia w statusie: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED) {
            throw new OrderDomainException(
                    "Nie można anulować zamówienia w statusie: " + this.status
            );
        }

        OrderStatus previousStatus = this.status;
        this.status = OrderStatus.CANCELLED;

        domainEvents.add(new OrderCancelledDomainEvent(
                this.id,
                this.customerId,
                reason,
                previousStatus
        ));
    }

    public Money getTotalAmount() {
        return items.calculateTotal();
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

}