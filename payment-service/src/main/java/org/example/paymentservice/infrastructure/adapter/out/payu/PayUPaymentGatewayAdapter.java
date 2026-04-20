package org.example.paymentservice.infrastructure.adapter.out.payu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentGatewayPort;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayUPaymentGatewayAdapter implements PaymentGatewayPort {

    private final PayUClient payUClient;

    @Override
    public String initiatePayment(Payment payment, String customerIp) {
        log.info("Gateway Adapter - Przygotowanie płatności w PayU dla zamówienia: {}", payment.getOrderId());

        int amountInCents = payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
        String orderIdStr = payment.getOrderId().toString();
        String paymentIdStr = payment.getId().toString();
        String currencyCode = payment.getCurrency();

        return payUClient.createPayment(
                orderIdStr,
                paymentIdStr,
                amountInCents,
                currencyCode,
                customerIp
        );
    }
}