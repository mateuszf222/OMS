package org.example.orderservice.application.port.in.cancelorder;

public interface CancelOrderByCustomerUseCase {
    void cancelOrderByCustomer(CancelOrderByCustomerCommand command);
}
