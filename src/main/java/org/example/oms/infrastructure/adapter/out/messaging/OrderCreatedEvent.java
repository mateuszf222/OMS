package org.example.oms.infrastructure.adapter.out.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency
) {}