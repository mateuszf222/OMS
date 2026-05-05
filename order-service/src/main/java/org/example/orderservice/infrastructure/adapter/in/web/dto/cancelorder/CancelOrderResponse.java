package org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder;

import java.util.UUID;

public record CancelOrderResponse(
        UUID orderId,
        String status,
        String message
) {}
