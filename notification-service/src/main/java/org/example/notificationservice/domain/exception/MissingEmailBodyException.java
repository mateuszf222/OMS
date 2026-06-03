package org.example.notificationservice.domain.exception;

public final class MissingEmailBodyException extends NotificationDomainException {

    public MissingEmailBodyException() {
        super("Email body cannot be blank.");
    }
}
