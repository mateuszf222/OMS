package org.example.orderservice.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId
) {}