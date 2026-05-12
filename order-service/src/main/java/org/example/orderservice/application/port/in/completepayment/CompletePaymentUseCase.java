package org.example.orderservice.application.port.in.completepayment;

public interface CompletePaymentUseCase {
    void completePayment(CompletePaymentCommand command);
}