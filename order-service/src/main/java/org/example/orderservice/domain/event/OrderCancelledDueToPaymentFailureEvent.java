package org.example.orderservice.domain.event;

import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;
import org.example.orderservice.domain.model.OrderStatus;

import java.util.UUID;

public record OrderCancelledDueToPaymentFailureEvent(
        UUID orderId,
        UUID customerId,
        UUID paymentId,
        PaymentFailureCancellationReason reason,
        OrderStatus previousStatus
) implements OrderCancellationEvent {
}
