package org.example.paymentservice.application.port.in;

public interface ProcessPaymentUseCase {
    void processPayment(ProcessPaymentCommand command);
}
