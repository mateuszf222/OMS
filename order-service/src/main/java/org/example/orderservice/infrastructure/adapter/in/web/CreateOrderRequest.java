package org.example.orderservice.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            UUID productId,
            int quantity,
            BigDecimal price,
            String currency
    ) {}
}