package org.example.paymentservice.application.port.in;

import java.util.UUID;

public interface HandlePaymentWebhookUseCase {
    void handleWebhook(UUID paymentId, GatewayPaymentStatus status);
}