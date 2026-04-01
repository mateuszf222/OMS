package org.example.orderservice.application.port.in;

import java.util.UUID;

public interface CancelOrderUseCase {
    void cancelOrder(UUID orderId, String reason);
}