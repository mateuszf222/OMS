package org.example.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements SendNotificationUseCase {

    private final JavaMailSender mailSender;

    @Override
    public void sendOrderCreatedNotification(UUID orderId, UUID customerId) {
        String to = customerId.toString() + "@dummy-domain.com";
        String subject = "Potwierdzenie przyjęcia zamówienia: " + orderId;
        String text = String.format("Drogi kliencie,\n\nTwoje zamówienie %s zostało przyjęte i oczekuje na płatność.", orderId);

        sendEmail(to, subject, text);
    }

    @Override
    public void sendPaymentSuccessNotification(UUID orderId) {
        String to = "klient-zamowienia-" + orderId + "@dummy-domain.com";
        String subject = "Płatność zakończona sukcesem dla zamówienia: " + orderId;
        String text = String.format("Płatność za zamówienie %s zakończona sukcesem. Przystępujemy do realizacji!", orderId);

        sendEmail(to, subject, text);
    }

    @Override
    public void sendPaymentFailedNotification(UUID orderId, String reason) {
        String to = "klient-zamowienia-" + orderId + "@dummy-domain.com";
        String subject = "Płatność odrzucona dla zamówienia: " + orderId;
        String text = String.format("Płatność za zamówienie %s została odrzucona.\nPowód: %s.\nZamówienie zostało anulowane.", orderId, reason);

        sendEmail(to, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sklep@enterprise-oms.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Pomyślnie wysłano e-mail do: {}", to);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania e-maila do: {}", to, e);
        }
    }
}