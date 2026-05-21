package org.example.orderservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCancelledDomainEvent;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.example.orderservice.domain.exception.CannotCancelOrderException;
import org.example.orderservice.domain.exception.CannotConfirmCancelledOrderException;
import org.example.orderservice.domain.exception.CustomerRequiredForOrderException;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.exception.OrderAlreadyConfirmedException;
import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;

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
        if (customerId == null) {
            throw new CustomerRequiredForOrderException();
        }
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
        if (!this.status.canTransitionTo(OrderStatus.CONFIRMED)) {
            if (this.status == OrderStatus.CONFIRMED) {
                throw new OrderAlreadyConfirmedException();
            }
            if (this.status == OrderStatus.CANCELLED) {
                throw new CannotConfirmCancelledOrderException();
            }
            throw new InvalidOrderStateTransitionException(this.status, OrderStatus.CONFIRMED, "confirm payment");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (!this.status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new CannotCancelOrderException(this.status);
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
