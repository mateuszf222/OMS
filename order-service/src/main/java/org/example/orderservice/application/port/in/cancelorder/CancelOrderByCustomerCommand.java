package org.example.orderservice.application.port.in.cancelorder;

import java.util.UUID;

import org.example.orderservice.domain.cancellation.CustomerCancellationReason;

public record CancelOrderByCustomerCommand(
        UUID orderId,
        UUID customerId,
        CustomerCancellationReason reason
) {}
