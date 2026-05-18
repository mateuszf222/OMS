package org.example.paymentservice.application.payment.port.in;

public interface ProcessPaymentUseCase {
    void processPayment(ProcessPaymentCommand command);
}
