package org.example.orderservice.application.port.in;

import java.util.UUID;

public record CancelOrderCommand(UUID orderId, String reason) {}