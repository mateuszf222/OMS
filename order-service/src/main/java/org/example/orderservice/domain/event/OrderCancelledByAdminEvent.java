package org.example.orderservice.domain.event;

import org.example.orderservice.domain.cancellation.AdminCancellationReason;
import org.example.orderservice.domain.model.OrderStatus;

import java.util.UUID;

public record OrderCancelledByAdminEvent(
        UUID orderId,
        UUID customerId,
        UUID adminId,
        AdminCancellationReason reason,
        OrderStatus previousStatus
) implements OrderCancellationEvent {
}
