package org.example.orderservice.application.port.in.cancelorder;

import java.util.UUID;

public record CancelOrderCommand(UUID orderId, String reason) {}