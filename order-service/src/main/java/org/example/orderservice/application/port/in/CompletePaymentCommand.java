package org.example.orderservice.application.port.in;

import java.util.UUID;

public record CompletePaymentCommand(
        UUID orderId,
        UUID paymentId
) {}