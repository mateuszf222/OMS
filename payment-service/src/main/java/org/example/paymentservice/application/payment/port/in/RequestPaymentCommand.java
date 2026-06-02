package org.example.paymentservice.application.payment.port.in;

import org.example.paymentservice.domain.model.Money;

import java.util.UUID;

public record RequestPaymentCommand(
        UUID orderId,
        UUID customerId,
        Money amount
) {}
