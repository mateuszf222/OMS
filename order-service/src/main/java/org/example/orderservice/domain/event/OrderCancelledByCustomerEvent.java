package org.example.orderservice.domain.event;

import org.example.orderservice.domain.cancellation.CustomerCancellationReason;
import org.example.orderservice.domain.model.OrderStatus;

import java.util.UUID;

public record OrderCancelledByCustomerEvent(
        UUID orderId,
        UUID customerId,
        CustomerCancellationReason reason,
        OrderStatus previousStatus
) implements OrderCancellationEvent {
}
