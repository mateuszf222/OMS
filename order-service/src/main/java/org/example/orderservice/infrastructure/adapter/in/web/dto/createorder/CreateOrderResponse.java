package org.example.orderservice.infrastructure.adapter.in.web.dto.createorder;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId
) {}