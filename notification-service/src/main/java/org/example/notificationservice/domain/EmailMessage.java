package org.example.notificationservice.domain;

public record EmailMessage(String to, String subject, String text) {
    public EmailMessage {
        if (to == null || !to.contains("@")) {
            throw new IllegalArgumentException("Nieprawidłowy format adresu e-mail: " + to);
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Temat wiadomości nie może być pusty");
        }
    }
}