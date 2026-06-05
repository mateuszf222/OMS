package org.example.orderservice.application.port.in.cancelorder;

public interface CancelOrderDueToPaymentFailureUseCase {
    void cancelOrderDueToPaymentFailure(CancelOrderDueToPaymentFailureCommand command);
}
