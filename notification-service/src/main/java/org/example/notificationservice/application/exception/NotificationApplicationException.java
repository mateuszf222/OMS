package org.example.notificationservice.application.exception;

public abstract sealed class NotificationApplicationException extends RuntimeException
        permits MissingNotificationDataException {

    protected NotificationApplicationException(String message) {
        super(message);
    }
}
