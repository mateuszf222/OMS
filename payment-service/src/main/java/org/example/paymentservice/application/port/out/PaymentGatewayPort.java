package org.example.paymentservice.application.port.out;

import org.example.paymentservice.domain.model.Payment;

public interface PaymentGatewayPort {
    String initiatePayment(Payment payment, PaymentGatewayOptions options);
}