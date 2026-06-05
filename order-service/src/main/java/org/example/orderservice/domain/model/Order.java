package org.example.orderservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.example.orderservice.domain.cancellation.AdminCancellationReason;
import org.example.orderservice.domain.cancellation.CustomerCancellationReason;
import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;
import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCancelledByAdminEvent;
import org.example.orderservice.domain.event.OrderCancelledByCustomerEvent;
import org.example.orderservice.domain.event.OrderCancelledDueToPaymentFailureEvent;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.example.orderservice.domain.exception.CustomerRequiredForOrderException;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public class Order {

    private static final OrderStatus INITIAL_STATUS = OrderStatus.PENDING;
    private static final OrderStatus PAID_STATUS = OrderStatus.CONFIRMED;
    private static final OrderStatus CANCELLED_STATUS = OrderStatus.CANCELLED;
    private static final String APPLY_SUCCESSFUL_PAYMENT_ACTION = "apply successful payment";
    private static final String CANCEL_ORDER_ACTION = "cancel order";

    private final UUID id;
    private final UUID customerId;
    private OrderStatus status;
    private final ZonedDateTime createdAt;
    private Long version;

    private final OrderLines items;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Order(UUID id, UUID customerId, OrderStatus status, OrderLines items, ZonedDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = items;
        this.createdAt = createdAt;
    }

    public static Order create(UUID customerId, List<OrderItem> items) {
        requireCustomer(customerId);

        Order order = new Order(
                UUID.randomUUID(),
                customerId,
                INITIAL_STATUS,
                new OrderLines(items),
                ZonedDateTime.now()
        );
        order.recordOrderCreated();

        return order;
    }

    public static Order restore(OrderState state) {
        Order order = new Order(
                state.id(),
                state.customerId(),
                state.status(),
                state.lines(),
                state.createdAt()
        );
        order.version = state.version();
        return order;
    }

    public void applySuccessfulPayment() {
        transitionTo(PAID_STATUS, APPLY_SUCCESSFUL_PAYMENT_ACTION);
    }

    public void cancelByCustomer(CustomerCancellationReason reason) {
        OrderStatus statusBeforeCancellation = this.status;

        transitionTo(CANCELLED_STATUS, CANCEL_ORDER_ACTION);
        record(new OrderCancelledByCustomerEvent(
                id,
                customerId,
                reason,
                statusBeforeCancellation
        ));
    }

    public void cancelByAdmin(UUID adminId, AdminCancellationReason reason) {
        OrderStatus statusBeforeCancellation = this.status;

        transitionTo(CANCELLED_STATUS, CANCEL_ORDER_ACTION);
        record(new OrderCancelledByAdminEvent(
                id,
                customerId,
                adminId,
                reason,
                statusBeforeCancellation
        ));
    }

    public void cancelDueToPaymentFailure(UUID paymentId, PaymentFailureCancellationReason reason) {
        OrderStatus statusBeforeCancellation = this.status;

        transitionTo(CANCELLED_STATUS, CANCEL_ORDER_ACTION);
        record(new OrderCancelledDueToPaymentFailureEvent(
                id,
                customerId,
                paymentId,
                reason,
                statusBeforeCancellation
        ));
    }

    public Money totalAmount() {
        return items.totalAmount();
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    private static void requireCustomer(UUID customerId) {
        if (customerId == null) {
            throw new CustomerRequiredForOrderException();
        }
    }

    private void transitionTo(OrderStatus targetStatus, String action) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStateTransitionException(this.status, targetStatus, action);
        }
        this.status = targetStatus;
    }

    private void recordOrderCreated() {
        record(new OrderCreatedDomainEvent(
                id,
                customerId,
                totalAmount()
        ));
    }

    private void record(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
    }
}
