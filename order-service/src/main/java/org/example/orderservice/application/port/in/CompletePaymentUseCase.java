package org.example.orderservice.application.port.in;

public interface CompletePaymentUseCase {
    void completePayment(CompletePaymentCommand command);
}