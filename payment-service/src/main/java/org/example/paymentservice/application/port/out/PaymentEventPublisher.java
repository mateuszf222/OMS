package org.example.paymentservice.application.port.out;

import org.example.paymentservice.domain.model.Payment;

public interface PaymentEventPublisher {
    void publishPaymentCompletedEvent(Payment savedPayment);
    void publishPaymentFailedEvent(Payment savedPayment);
}
