package org.example.orderservice.application.port.in.cancelorder;

public interface CancelOrderByAdminUseCase {
    void cancelOrderByAdmin(CancelOrderByAdminCommand command);
}
