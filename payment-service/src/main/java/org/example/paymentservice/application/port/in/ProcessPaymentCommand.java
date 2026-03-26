package org.example.paymentservice.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
        UUID orderId,
        BigDecimal amount,
        String currency
) {}