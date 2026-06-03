package org.example.notificationservice.domain.exception;

public final class MissingEmailSubjectException extends NotificationDomainException {

    public MissingEmailSubjectException() {
        super("Email subject cannot be blank.");
    }
}
