package org.example.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
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
        String to = customerId.toString() + "@dummy-domain.com";
        String subject = "Potwierdzenie przyjęcia zamówienia: " + orderId;
        String text = String.format("Drogi kliencie,\n\nTwoje zamówienie %s zostało przyjęte i oczekuje na płatność.", orderId);

        emailSenderAdapter.sendEmail(to, subject, text);
    }

    @Override
    public void sendPaymentSuccessNotification(UUID orderId) {
        String to = "klient-zamowienia-" + orderId + "@dummy-domain.com";
        String subject = "Płatność zakończona sukcesem dla zamówienia: " + orderId;
        String text = String.format("Płatność za zamówienie %s zakończona sukcesem. Przystępujemy do realizacji!", orderId);

        emailSenderAdapter.sendEmail(to, subject, text);
    }

    @Override
    public void sendPaymentFailedNotification(UUID orderId, String reason) {
        String to = "klient-zamowienia-" + orderId + "@dummy-domain.com";
        String subject = "Płatność odrzucona dla zamówienia: " + orderId;
        String text = String.format("Płatność za zamówienie %s została odrzucona.\nPowód: %s.\nZamówienie zostało anulowane.", orderId, reason);

        emailSenderAdapter.sendEmail(to, subject, text);
    }
}