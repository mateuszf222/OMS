package org.example.orderservice.application.port.in;

public interface CancelOrderUseCase {
    void cancelOrder(CancelOrderCommand command);
}