package org.example.notificationservice.application.port.in;

import java.util.UUID;

public interface SendNotificationUseCase {
    void sendOrderCreatedNotification(UUID orderId, UUID customerId);
    void sendPaymentSuccessNotification(UUID orderId, UUID customerId);
    void sendPaymentFailedNotification(UUID orderId, UUID customerId, String reason);
}