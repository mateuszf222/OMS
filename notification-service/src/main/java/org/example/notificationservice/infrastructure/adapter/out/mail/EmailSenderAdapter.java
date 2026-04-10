package org.example.notificationservice.infrastructure.adapter.out.mail;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderAdapter {

    private final JavaMailSender mailSender;

    @Retry(name = "mailtrapRetry", fallbackMethod = "sendEmailFallback")
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sklep@enterprise-oms.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Pomyślnie wysłano e-mail do: {}", to);
    }

    public void sendEmailFallback(String to, String subject, String text, MailException ex) {
        log.error("Nie udało się wysłać e-maila do {} pomimo 5 prób ponowienia. Przyczyna: {}", to, ex.getMessage());
    }
}