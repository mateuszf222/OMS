package org.example.orderservice.application.port.in.createorder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        List<OrderItemCommand> items
) {
    public record OrderItemCommand(
            UUID productId,
            int quantity,
            BigDecimal price,
            Currency currency
    ) {}
}