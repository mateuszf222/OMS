package org.example.paymentservice.application.port.in;

import org.example.paymentservice.domain.model.Money;
import java.util.UUID;

public record ProcessPaymentCommand(
        UUID orderId,
        UUID customerId,
        Money amount
) {}