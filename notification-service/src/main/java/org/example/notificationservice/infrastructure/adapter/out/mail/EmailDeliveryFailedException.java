package org.example.notificationservice.infrastructure.adapter.out.mail;

import org.example.notificationservice.domain.EmailMessage;

public final class EmailDeliveryFailedException extends RuntimeException {

    public EmailDeliveryFailedException(EmailMessage email, Throwable cause) {
        super("Email delivery failed for recipient: " + recipient(email), cause);
    }

    private static String recipient(EmailMessage email) {
        return email == null ? "<unknown>" : email.to();
    }
}
