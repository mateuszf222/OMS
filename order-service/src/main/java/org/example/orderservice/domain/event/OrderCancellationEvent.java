package org.example.orderservice.domain.event;

import org.example.orderservice.domain.cancellation.CancellationReason;
import org.example.orderservice.domain.model.OrderStatus;

import java.util.UUID;

public sealed interface OrderCancellationEvent extends DomainEvent permits
        OrderCancelledByAdminEvent,
        OrderCancelledByCustomerEvent,
        OrderCancelledDueToPaymentFailureEvent {

    UUID orderId();

    UUID customerId();

    CancellationReason reason();

    OrderStatus previousStatus();
}
