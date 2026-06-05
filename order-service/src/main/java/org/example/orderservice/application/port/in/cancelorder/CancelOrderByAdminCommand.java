package org.example.orderservice.application.port.in.cancelorder;

import java.util.UUID;

import org.example.orderservice.domain.cancellation.AdminCancellationReason;

public record CancelOrderByAdminCommand(
        UUID orderId,
        UUID adminId,
        AdminCancellationReason reason
) {}
