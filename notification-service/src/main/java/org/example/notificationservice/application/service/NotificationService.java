package org.example.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.example.notificationservice.domain.EmailMessage;
import org.example.notificationservice.infrastructure.adapter.out.mail.EmailSenderAdapter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements SendNotificationUseCase {

    private final EmailSenderAdapter emailSenderAdapter;

    @Override
    public void sendOrderCreatedNotification(UUID orderId, UUID customerId) {
        EmailMessage message = new EmailMessage(
                customerId.toString() + "@dummy-domain.com",
                "Potwierdzenie przyjęcia zamówienia: " + orderId,
                String.format("Drogi kliencie,\n\nTwoje zamówienie %s zostało przyjęte i oczekuje na płatność.", orderId)
        );
        emailSenderAdapter.sendEmail(message);
    }

    @Override
    public void sendPaymentSuccessNotification(UUID orderId, UUID customerId) {
        EmailMessage message = new EmailMessage(
                customerId.toString() + "@dummy-domain.com",
                "Płatność zakończona sukcesem dla zamówienia: " + orderId,
                String.format("Płatność za zamówienie %s zakończona sukcesem. Przystępujemy do realizacji!", orderId)
        );
        emailSenderAdapter.sendEmail(message);
    }

    @Override
    public void sendPaymentFailedNotification(UUID orderId, UUID customerId, String reason) {
        EmailMessage message = new EmailMessage(
                customerId.toString() + "@dummy-domain.com",
                "Płatność odrzucona dla zamówienia: " + orderId,
                String.format("Płatność za zamówienie %s została odrzucona.\nPowód: %s.\nZamówienie zostało anulowane.", orderId, reason)
        );
        emailSenderAdapter.sendEmail(message);
    }
}