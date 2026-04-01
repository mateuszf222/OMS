package org.example.orderservice.application.port.in;

import java.util.UUID;

public interface CompletePaymentUseCase {
    void completePayment(UUID orderId);
}