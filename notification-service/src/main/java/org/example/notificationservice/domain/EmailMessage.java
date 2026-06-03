package org.example.notificationservice.domain;

import org.example.notificationservice.domain.exception.InvalidEmailRecipientException;
import org.example.notificationservice.domain.exception.MissingEmailBodyException;
import org.example.notificationservice.domain.exception.MissingEmailSubjectException;

public record EmailMessage(String to, String subject, String text) {

    public EmailMessage {
        if (to == null || !to.contains("@")) {
            throw new InvalidEmailRecipientException(to);
        }
        if (subject == null || subject.isBlank()) {
            throw new MissingEmailSubjectException();
        }
        if (text == null || text.isBlank()) {
            throw new MissingEmailBodyException();
        }
    }
}
