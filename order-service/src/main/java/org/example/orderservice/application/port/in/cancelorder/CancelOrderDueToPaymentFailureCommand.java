package org.example.orderservice.application.port.in.cancelorder;

import java.util.UUID;

import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;

public record CancelOrderDueToPaymentFailureCommand(
        UUID orderId,
        UUID paymentId,
        PaymentFailureCancellationReason reason
) {}
