package org.example.paymentservice.infrastructure.adapter.out.payu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentGatewayOptions;
import org.example.paymentservice.application.port.out.PaymentGatewayPort;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayUPaymentGatewayAdapter implements PaymentGatewayPort {

    private final PayUClient payUClient;

    @Override
    public String initiatePayment(Payment payment, PaymentGatewayOptions options) {
        log.info("Gateway Adapter - Przygotowanie płatności w PayU dla zamówienia: {}", payment.getOrderId());

        return payUClient.createPayment(payment, options);
    }
}