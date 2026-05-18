package org.example.paymentservice.application.payment.port.out;

import org.example.paymentservice.domain.model.payment.Payment;

public interface PaymentGatewayPort {
    String initiatePayment(Payment payment, PaymentGatewayOptions options);
}