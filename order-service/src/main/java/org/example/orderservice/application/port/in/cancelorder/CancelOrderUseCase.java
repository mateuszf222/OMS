package org.example.orderservice.application.port.in.cancelorder;

public interface CancelOrderUseCase {
    void cancelOrder(CancelOrderCommand command);
}