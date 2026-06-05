package org.example.orderservice.domain.model.assertion;

import org.example.orderservice.domain.model.OrderStatus;

import org.example.orderservice.domain.model.Order;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCancellationEvent;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    private static final String TOTAL_VALUE_SEPARATOR = "\\s+";

    private boolean returnPulledDomainEvents = true;

    private OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    public OrderAssert hasId(UUID expected) {
        isNotNull();

        Assertions.assertThat(actual.getId())
                .as("order id")
                .isEqualTo(expected);
        return this;
    }

    public OrderAssert hasCustomerId(UUID expected) {
        return belongsTo(expected);
    }

    public OrderAssert belongsTo(UUID customerId) {
        isNotNull();

        Assertions.assertThat(actual.getCustomerId())
                .as("order customer id")
                .isEqualTo(customerId);
        return this;
    }

    public OrderAssert hasStatus(OrderStatus status) {
        isNotNull();

        Assertions.assertThat(actual.getStatus())
                .as("order status")
                .isEqualTo(status);
        return this;
    }

    public OrderAssert isPending() {
        return hasStatus(OrderStatus.PENDING);
    }

    public OrderAssert isPendingOrder() {
        return isPending();
    }

    public OrderAssert isConfirmed() {
        return hasStatus(OrderStatus.CONFIRMED);
    }

    public OrderAssert isCancelled() {
        return hasStatus(OrderStatus.CANCELLED);
    }

    public OrderAssert hasTotalAmount(BigDecimal amount, Currency currency) {
        isNotNull();

        MoneyAssert.assertThat(actual.totalAmount())
                .hasValue(amount, currency);
        return this;
    }

    public OrderAssert hasTotalAmount(String amount, String currency) {
        return hasTotalAmount(new BigDecimal(amount), Currency.getInstance(currency));
    }

    public OrderAssert hasTotalValueOf(String totalValue) {
        String[] parts = totalValue.strip().split(TOTAL_VALUE_SEPARATOR);
        if (parts.length != 2) {
            failWithMessage("Expected total value in '<amount> <currency>' format, but was <%s>", totalValue);
        }

        return hasTotalAmount(parts[0], parts[1]);
    }

    public OrderAssert hasNumberOfItems(int expectedCount) {
        isNotNull();

        Assertions.assertThat(actual.getItems().toList())
                .as("order items")
                .hasSize(expectedCount);
        return this;
    }

    public ItemCountAssert hasExactly(int expectedCount) {
        isNotNull();
        return new ItemCountAssert(this, expectedCount);
    }

    public OrderAssert containsProduct(UUID productId) {
        isNotNull();

        Assertions.assertThat(actual.getItems().toList())
                .as("order items")
                .anySatisfy(item -> Assertions.assertThat(item.getProductId()).isEqualTo(productId));
        return this;
    }

    public OrderAssert hasDomainEvent(Class<? extends DomainEvent> eventType) {
        isNotNull();

        Assertions.assertThat(pullDomainEvents())
                .as("order domain events")
                .anySatisfy(event -> Assertions.assertThat(event).isInstanceOf(eventType));
        return this;
    }

    public OrderAssert emittedEvent(Class<? extends DomainEvent> eventType) {
        return hasDomainEvent(eventType);
    }

    public OrderAssert emittedCancellationBecause(String reason) {
        isNotNull();

        Assertions.assertThat(pullCancellationEvents())
                .as("order cancellation events")
                .anySatisfy(event -> Assertions.assertThat(event.reason().value())
                        .as("cancellation reason")
                        .isEqualTo(reason));
        return this;
    }

    public OrderAssert emittedCancellationFrom(OrderStatus previousStatus) {
        isNotNull();

        Assertions.assertThat(pullCancellationEvents())
                .as("order cancellation events")
                .anySatisfy(event -> Assertions.assertThat(event.previousStatus())
                        .as("previous order status")
                        .isEqualTo(previousStatus));
        return this;
    }

    public OrderAssert hasNoDomainEvents() {
        isNotNull();

        Assertions.assertThat(pullDomainEvents())
                .as("order domain events")
                .isEmpty();
        return this;
    }

    public OrderAssert returningDomainEventsBack() {
        this.returnPulledDomainEvents = true;
        return this;
    }

    public OrderAssert preservingDomainEvents() {
        return returningDomainEventsBack();
    }

    public OrderAssert consumingDomainEvents() {
        this.returnPulledDomainEvents = false;
        return this;
    }

    public OrderAssert wasCreatedAt(Instant time) {
        isNotNull();

        Assertions.assertThat(actual.getCreatedAt().toInstant())
                .as("order created at")
                .isEqualTo(time);
        return this;
    }

    private List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = actual.pullDomainEvents();

        if (returnPulledDomainEvents) {
            returnDomainEvents(events);
        }

        return events;
    }

    private List<OrderCancellationEvent> pullCancellationEvents() {
        return pullDomainEvents().stream()
                .filter(OrderCancellationEvent.class::isInstance)
                .map(OrderCancellationEvent.class::cast)
                .toList();
    }

    private void returnDomainEvents(List<DomainEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        try {
            Field field = Order.class.getDeclaredField("domainEvents");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<DomainEvent> domainEvents = (List<DomainEvent>) field.get(actual);
            domainEvents.addAll(events);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not return pulled domain events to the aggregate.", e);
        }
    }

    public static final class ItemCountAssert {
        private final OrderAssert parent;
        private final int expectedCount;

        private ItemCountAssert(OrderAssert parent, int expectedCount) {
            this.parent = parent;
            this.expectedCount = expectedCount;
        }

        public OrderAssert items() {
            return parent.hasNumberOfItems(expectedCount);
        }
    }
}

