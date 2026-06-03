package org.example.notificationservice.domain.exception;

public abstract sealed class NotificationDomainException extends RuntimeException
        permits InvalidEmailRecipientException, MissingEmailBodyException, MissingEmailSubjectException {

    protected NotificationDomainException(String message) {
        super(message);
    }
}
