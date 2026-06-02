package org.example.paymentservice.application.payment.port.in;

public interface RequestPaymentUseCase {
    void requestPayment(RequestPaymentCommand command);
}
