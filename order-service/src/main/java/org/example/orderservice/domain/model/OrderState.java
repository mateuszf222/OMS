package org.example.orderservice.domain.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record OrderState(
        UUID id,
        UUID customerId,
        OrderStatus status,
        OrderLines lines,
        ZonedDateTime createdAt,
        Long version
) {}