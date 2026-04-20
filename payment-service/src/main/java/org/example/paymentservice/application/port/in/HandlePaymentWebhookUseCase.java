package org.example.paymentservice.application.port.in;
import org.example.paymentservice.infrastructure.adapter.in.web.PayUStatus;

import java.util.UUID;

public interface HandlePaymentWebhookUseCase {
    void handleWebhook(UUID paymentId, PayUStatus externalStatus);
}