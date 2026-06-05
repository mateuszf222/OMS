package org.example.orderservice.application.port.in.createorder;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        List<OrderItemCommand> items
) {
    public record OrderItemCommand(
            UUID productId,
            int quantity
    ) {}
}
