package org.example.paymentservice.application.payment.port.in;

import java.util.UUID;

public interface ApplyGatewayPaymentStatusUseCase {
    void applyGatewayPaymentStatus(UUID paymentId, GatewayPaymentStatus status);
}
