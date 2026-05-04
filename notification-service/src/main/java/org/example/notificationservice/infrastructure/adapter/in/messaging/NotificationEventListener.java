package org.example.notificationservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SendNotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvents(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            notificationUseCase.sendOrderCreatedNotification(event.orderId(), event.customerId());
        } catch (Exception e) {
            log.error("Błąd przetwarzania zdarzenia z order-events: {}", payload, e);
        }
    }

    @KafkaListener(topics = "payment-completed-events", groupId = "notification-service-group")
    public void handlePaymentCompletedEvents(String payload) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
            notificationUseCase.sendPaymentSuccessNotification(event.orderId(), event.customerId());
        } catch (Exception e) {
            log.error("Błąd przetwarzania zdarzenia z payment-completed-events: {}", payload, e);
        }
    }

    @KafkaListener(topics = "payment-failed-events", groupId = "notification-service-group")
    public void handlePaymentFailedEvents(String payload) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
            notificationUseCase.sendPaymentFailedNotification(event.orderId(), event.customerId(), event.reason());
        } catch (Exception e) {
            log.error("Błąd przetwarzania zdarzenia z payment-failed-events: {}", payload, e);
        }
    }
}